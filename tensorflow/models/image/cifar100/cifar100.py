from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import gzip
import os
import re
import sys
import tarfile

from six.moves import urllib
import tensorflow as tf

import cifar100_input

FLAGS = tf.app.flags.FLAGS


# Basic model parameters.
tf.app.flags.DEFINE_integer('batch_size', 128,
                            """Number of images to process in a batch.""")
tf.app.flags.DEFINE_string('data_dir', '/tmp/cifar100_data',
                           """Path to the CIFAR-10 data directory.""")
tf.app.flags.DEFINE_boolean('use_fp16', False,
                            """Train the model using fp16.""")

# Global constants describing the CIFAR-10 data set.
IMAGE_SIZE = cifar100_input.IMAGE_SIZE
NUM_CLASSES = cifar100_input.NUM_CLASSES
NUM_EXAMPLES_PER_EPOCH_FOR_TRAIN = cifar100_input.NUM_EXAMPLES_PER_EPOCH_FOR_TRAIN
NUM_EXAMPLES_PER_EPOCH_FOR_EVAL = cifar100_input.NUM_EXAMPLES_PER_EPOCH_FOR_EVAL

# Constants describing the training process.
MOVING_AVERAGE_DECAY = 0.9999     # The decay to use for the moving average.
NUM_EPOCHS_PER_DECAY = 350.0      # Epochs after which learning rate decays.
LEARNING_RATE_DECAY_FACTOR = 0.1  # Learning rate decay factor.
INITIAL_LEARNING_RATE = 0.1       # Initial learning rate.


DATA_URL = "https://www.cs.toronto.edu/~kriz/cifar-100-binary.tar.gz"


def inputs(input_method):
    data_dir = os.path.join(FLAGS.data_dir, 'cifar-100-binary')
    images, labels = input_method(data_dir, FLAGS.batch_size)
    return images, labels


def distorted_inputs():
    return inputs(cifar100_input.distorted_inputs)


def eval_inputs():
    return inputs(cifar100_input.eval_inputs, )


def weight_variable(shape, stddev, wd):
    initial = tf.truncated_normal(shape, stddev=stddev, dtype=tf.float32)
    var = tf.Variable(initial)
    if wd is not None:
        weight_decay = tf.mul(tf.nn.l2_loss(var), wd, name='weight_loss')
        tf.add_to_collection('losses', weight_decay)
    return var


def bias_variable(shape):
    initial = tf.constant(0.1, shape=shape)
    return tf.Variable(initial)


def conv2d(x, W):
    return tf.nn.conv2d(x, W, strides=[1, 1, 1, 1], padding='SAME')


def max_pool_3x3(x):
    return tf.nn.max_pool(x, ksize=[1, 3, 3, 1],
                          strides=[1, 2, 2, 1], padding='SAME')


def inference(images):
    #Conv1
    W_conv1 = weight_variable([5, 5, 3, 64], 5e-2, 0.0)
    W_bias1 = bias_variable([64])
    h_conv1 = tf.nn.relu(conv2d(images, W_conv1) + W_bias1)

    #maxppool1
    h_pool1 = max_pool_3x3(h_conv1)

    #norm 1
    norm1 = tf.nn.lrn(h_pool1, 4, bias=1.0, alpha=0.001 / 9.0, beta=0.75)


    #Conv2
    W_conv2 = weight_variable([5, 5, 64, 64], 5e-2, 0.0)
    W_bias2 = bias_variable([64])
    h_conv2 = tf.nn.relu(conv2d(norm1, W_conv2) + W_bias2)

    #norm 2
    norm2 = tf.nn.lrn(h_conv2, 4, bias=1.0, alpha=0.001 / 9.0, beta=0.75)

    #maxpool 2
    h_pool2 = max_pool_3x3(norm2)


    #local 3
    reshape = tf.reshape(h_pool2, [FLAGS.batch_size, -1])
    dim = reshape.get_shape()[1].value
    weights_3 = weight_variable([dim, 768], 0.04, 0.004)
    biases_3 = bias_variable([768])
    local3 = tf.nn.relu(tf.matmul(reshape, weights_3) + biases_3)

    #local 4
    weights_4 = weight_variable([768, 384], 0.04, 0.004)
    biases_4 = bias_variable([384])
    local4 = tf.nn.relu(tf.matmul(local3, weights_4) + biases_4)

    weights_final = weight_variable([384, NUM_CLASSES], 1/384.0, 0.0)
    biases_final = bias_variable([NUM_CLASSES])
    softmax_linear = tf.add(tf.matmul(local4, weights_final), biases_final)

    return softmax_linear


def loss(logits, labels):
    labels = tf.cast(labels, tf.int64)
    cross_entropy = tf.nn.sparse_softmax_cross_entropy_with_logits(
        logits, labels, name='cross_entropy_per_example')
    cross_entropy_mean = tf.reduce_mean(cross_entropy, name='cross_entropy')
    tf.add_to_collection('losses', cross_entropy_mean)

    # The total loss is defined as the cross entropy loss plus all of the weight
    # decay terms (L2 loss).
    return tf.add_n(tf.get_collection('losses'), name='total_loss')


def train(total_loss, global_step):
    num_batches_per_epoch = NUM_EXAMPLES_PER_EPOCH_FOR_TRAIN / FLAGS.batch_size
    decay_steps = int(num_batches_per_epoch * NUM_EPOCHS_PER_DECAY)
    lr = tf.train.exponential_decay(INITIAL_LEARNING_RATE,
                                    global_step,
                                    decay_steps,
                                    LEARNING_RATE_DECAY_FACTOR,
                                    staircase=True)
    opt = tf.train.GradientDescentOptimizer(lr)
    grads = opt.compute_gradients(total_loss)

    apply_gradient_op = opt.apply_gradients(grads, global_step=global_step)

    return apply_gradient_op



def download_files_and_extrace():
    data_directory = FLAGS.data_dir
    if not os.path.exists(data_directory):
        os.makedirs(data_directory)
    filename = DATA_URL.split('/')[-1]
    filepath = os.path.join(data_directory, filename)
    if not os.path.exists(filepath):
        def _progress(count, block_size, total_size):
            sys.stdout.write('\r>> Downloading %s %.1f%%' % (filename,
                                                             float(count * block_size) / float(total_size) * 100.0))
            sys.stdout.flush()

        filepath, _ = urllib.request.urlretrieve(DATA_URL, filepath, _progress)
        print()
        statinfo = os.stat(filepath)
        print('Successfully downloaded', filename, statinfo.st_size, 'bytes.')
        tarfile.open(filepath, 'r:gz').extractall(data_directory)
