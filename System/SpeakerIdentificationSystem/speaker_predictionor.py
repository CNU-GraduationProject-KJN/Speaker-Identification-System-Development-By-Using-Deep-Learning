from tensorflow.keras.callbacks import ReduceLROnPlateau
from tensorflow.keras.utils import to_categorical
import tensorflow.keras.backend as K
from tensorflow.keras import regularizers
from tensorflow.keras.layers import Lambda, Bidirectional, LSTM
from tensorflow.keras.layers import Conv1D, MaxPooling1D
from tensorflow.keras.layers import Activation, Dense
from tensorflow.keras.layers import BatchNormalization
from tensorflow.keras.models import Sequential
from tensorflow import convert_to_tensor
from tensorflow import expand_dims
from keras.models import load_model
import numpy as np
import pickle
import os
from glob import glob
import tensorflow

DATA_AUDIO_DIR = './train_data'
list_dir = os.listdir(DATA_AUDIO_DIR)
list_dir.sort()

class_ids = {list_dir[i]: i for i in range(len(list_dir))}

def m5(num_classes):
    print("Class Num", num_classes)
    print('Using Model M5')
    m = Sequential()
    m.add(Conv1D(128,
                 input_shape=[AUDIO_LENGTH, 1],
                 kernel_size=80,
                 strides=4,
                 padding='same',
                 kernel_initializer='glorot_uniform',
                 kernel_regularizer=regularizers.l2(l=0.0001)))
    m.add(BatchNormalization())
    m.add(Activation('relu'))
    m.add(MaxPooling1D(pool_size=4, strides=None))
    m.add(Conv1D(128,
                 kernel_size=3,
                 strides=1,
                 padding='same',
                 kernel_initializer='glorot_uniform',
                 kernel_regularizer=regularizers.l2(l=0.0001)))
    m.add(BatchNormalization())
    m.add(Activation('relu'))
    m.add(MaxPooling1D(pool_size=4, strides=None))
    m.add(Conv1D(256,
                 kernel_size=3,
                 strides=1,
                 padding='same',
                 kernel_initializer='glorot_uniform',
                 kernel_regularizer=regularizers.l2(l=0.0001)))
    m.add(BatchNormalization())
    m.add(Activation('relu'))
    m.add(MaxPooling1D(pool_size=4, strides=None))
    m.add(Conv1D(512,
                 kernel_size=3,
                 strides=1,
                 padding='same',
                 kernel_initializer='glorot_uniform',
                 kernel_regularizer=regularizers.l2(l=0.0001)))
    m.add(BatchNormalization())
    m.add(Activation('relu'))
    m.add(MaxPooling1D(pool_size=4, strides=None))
    m.add(Lambda(lambda x: K.mean(x, axis=1)))  # Same as GAP for 1D Conv Layer
    m.add(Dense(num_classes, activation='softmax'))
    return m


def get_data(file_list):
    def load_into(_filename, _x, _y):
        with open(_filename, 'rb') as f:
            audio_element = pickle.load(f)
            _x.append(audio_element['audio'])
            _y.append(int(audio_element['class_id']))

    x, y = [], []
    for filename in file_list:
        load_into(filename, x, y)
    return np.array(x), np.array(y)


def model_training():
    
    num_classes = len(list_dir)
    model = m5(num_classes)
    
    if model is None:
        exit('Something went wrong!!')

    model.compile(optimizer='adam',
                  loss='categorical_crossentropy',
                  metrics=['accuracy'])
    print(model.summary())


    train_files = glob(os.path.join(OUTPUT_DIR_TRAIN, '**.pkl'))
    print(os.path.join(OUTPUT_DIR_TRAIN, '**.pkl'))
    x_tr, y_tr = get_data(train_files)
    y_tr = to_categorical(y_tr, num_classes=num_classes)

    test_files = glob(os.path.join(OUTPUT_DIR_TEST, '**.pkl'))
    x_te, y_te = get_data(test_files)
    y_te = to_categorical(y_te, num_classes=num_classes)

    # if the accuracy does not increase over 10 epochs, reduce the learning rate by half.
    reduce_lr = ReduceLROnPlateau(monitor='val_accuracy', factor=0.5, patience=10, min_lr=0.00005, verbose=1)
    batch_size = 128
    history = model.fit(x=x_tr, y=y_tr, batch_size=16, epochs=100, verbose=2, shuffle=True, validation_data=(x_te, y_te), callbacks=[reduce_lr])
    model.save('Identity_Predictionor.h5')

def model_testing():
    val_files = glob(os.path.join(OUTPUT_DIR_RESAMPLED_VAL_TRAIN, '**.pkl'))
    val_files.sort()

    x_val, y_val = get_data(val_files)
    x_val = x_val.reshape(-1, x_val.shape[1], 1)


    model = load_model('Identity_Predictionor.h5')

    yhat = model.predict(x_val)

    for i,pred in enumerate(yhat) :
        if str(np.max(pred) < 0.95:
            return None
        else:
            return str(list_dir[np.argmax(pred)])