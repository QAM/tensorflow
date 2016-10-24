from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import os

from six.moves import xrange  # pylint: disable=redefined-builtin
import tensorflow as tf


IMAGE_SIZE=24

NUM_CLASSES = 20
NUM_EXAMPLES_PER_EPOCH_FOR_TRAIN = 50000
NUM_EXAMPLES_PER_EPOCH_FOR_EVAL = 10000


def _read_image(filename_queue):
    label_bytes = 2
    height = 32
    width = 32
    depth = 3
    image_bytes = height*width*depth
    record_bytes = image_bytes+label_bytes

    reader = tf.FixedLengthRecordReader(record_bytes=record_bytes)
    key, value = reader.read(filename_queue)
    record_bytes = tf.decode_raw(value, tf.uint8)
    label = tf.cast(
        tf.slice(record_bytes, [0], [label_bytes-1]), tf.int32
    )
    unreshape_image = tf.reshape(
        tf.slice(record_bytes, [label_bytes], [image_bytes]),
        [depth, height, width]
    )
    result_image = tf.transpose(unreshape_image, [1, 2, 0])
    return result_image, label


def _generat_batch(image, label, min_queue_size, batch_size, shuffle):
    num_preprocess_threads = 16
    if shuffle:
        images, labels = tf.train.shuffle_batch([image, label],
                                                batch_size=batch_size,
                                                num_threads=num_preprocess_threads,
                                                capacity=min_queue_size + 3*batch_size,
                                                min_after_dequeue=min_queue_size)
    else:
        images, labels = tf.train.batch([image, label],
                                        batch_size=batch_size,
                                        num_threads=num_preprocess_threads,
                                        capacity=min_queue_size + 3*batch_size)

    return images, tf.reshape(labels, [batch_size])


def distorted_inputs(data_dir, batch_size):
    filenames = [os.path.join(data_dir, "train.bin")]
    for f in filenames:
        print(f)
        if not tf.gfile.Exists(f):
            raise ValueError('Failed to find file: ' + f)
    filename_queue = tf.train.string_input_producer(filenames)
    read_image, read_label = _read_image(filename_queue)
    reshaped_image = tf.cast(read_image, tf.float32)

    height = IMAGE_SIZE
    width = IMAGE_SIZE

    distorted_image = tf.random_crop(reshaped_image, [height, width, 3])
    distorted_image = tf.image.random_flip_left_right(distorted_image)
    distorted_image = tf.image.random_brightness(distorted_image,
                                                 max_delta=63)
    distorted_image = tf.image.random_contrast(distorted_image,
                                               lower=0.2, upper=1.8)
    float_image = tf.image.per_image_whitening(distorted_image)

    min_queue_examples = int(NUM_EXAMPLES_PER_EPOCH_FOR_TRAIN * 0.4)
    return _generat_batch(float_image, read_label, min_queue_examples, batch_size, True)


def eval_inputs(data_dir, batch_size):
    filenames = [os.path.join(data_dir, "test.bin")]
    for f in filenames:
        print(f)
        if not tf.gfile.Exists(f):
            raise ValueError('Failed to find file: ' + f)
    filename_queue = tf.train.string_input_producer(filenames)
    read_image, read_label = _read_image(filename_queue)
    reshaped_image = tf.cast(read_image, tf.float32)

    height = IMAGE_SIZE
    width = IMAGE_SIZE

    distorted_image = tf.image.resize_image_with_crop_or_pad(reshaped_image,
                                                           width, height)
    float_image = tf.image.per_image_whitening(distorted_image)

    min_queue_examples = int(NUM_EXAMPLES_PER_EPOCH_FOR_TRAIN * 0.4)
    return _generat_batch(float_image, read_label, min_queue_examples, batch_size, True)