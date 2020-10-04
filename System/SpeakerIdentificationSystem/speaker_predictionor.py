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
from tensorflow.keras.models import load_model
import numpy as np
import pickle
import os
from glob import glob
import tensorflow

from DB.db_controller import get_member_count_from_db, get_member_key_from_db, get_member_name_from_db

DATA_AUDIO_DIR = '/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/unzipfiles'

# list_dir = os.listdir(DATA_AUDIO_DIR)
# list_dir.sort()

# class_ids = {list_dir[i]: i for i in range(len(list_dir))}

TARGET_SR = 8000
AUDIO_LENGTH = TARGET_SR * 3

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


def model_training(path, key_list):
    num_classes = get_member_count_from_db()
    x_tr, y_tr = np.array([]), np.array([])
    x_te, y_te = np.array([]), np.array([])

    for key in key_list:
        OUTPUT_DIR_TRAIN = path + '/' + key + '/preprocessed/pickle/train'
        OUTPUT_DIR_TEST = path + '/' + key + '/preprocessed/pickle/test'

        train_files = glob(os.path.join(OUTPUT_DIR_TRAIN, '**.pkl'))
        # print(os.path.join(OUTPUT_DIR_TRAIN, '**.pkl'))
        x_tr_temp, y_tr_temp = get_data(train_files)
        x_tr=np.append(x_tr,x_tr_temp)
        y_tr=np.append(y_tr,y_tr_temp)

        test_files = glob(os.path.join(OUTPUT_DIR_TEST, '**.pkl'))
        x_te_temp, y_te_temp = get_data(test_files)
        x_te=np.append(x_te,x_te_temp)
        y_te=np.append(y_te,y_te_temp)

    x_tr = np.reshape(x_tr,(-1,AUDIO_LENGTH,1))
    x_te = np.reshape(x_te, (-1, AUDIO_LENGTH, 1))
    y_tr = to_categorical(y_tr, num_classes=num_classes)
    y_te = to_categorical(y_te, num_classes=num_classes)

    model = m5(num_classes)

    if model is None:
        exit('Something went wrong!!')

    model.compile(optimizer='adam',
                  loss='categorical_crossentropy',
                  metrics=['accuracy'])
    print(model.summary())

    print("x_tr: ", x_tr.shape)
    print("y_tr: ", y_tr.shape)

    print("x_te: ", x_te.shape)
    print("y_te: ", y_te.shape)
    # if the accuracy does not increase over 10 epochs, reduce the learning rate by half.
    reduce_lr = ReduceLROnPlateau(monitor='val_accuracy', factor=0.5, patience=10, min_lr=0.00005, verbose=1)
    model.fit(x=x_tr, y=y_tr, batch_size=16, epochs=50, verbose=2, shuffle=True,
                        validation_data=(x_te, y_te), callbacks=[reduce_lr])
    model.save('Identity_Predictionor.h5')


def model_testing():
    data_dir = '/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/val'
    key = os.listdir(data_dir)[0]
    path = data_dir+'/'+key


    val_files = glob(os.path.join(path+'/preprocessed/pickle/', '**.pkl'))
    val_files.sort()
    print(val_files)

    x_val, y_val = get_data(val_files)
    x_val = x_val.reshape(-1, AUDIO_LENGTH, 1)

    model = load_model('Identity_Predictionor.h5')

    yhat = model.predict(x_val)

    for i,pred in enumerate(yhat):
        if np.max(pred) < 0.95:
            predict_key = get_member_key_from_db(str(np.argmax(pred)))
            return '미지의 화자 두둥 '+str(np.max(pred)*100)+'% '+ str(get_member_name_from_db(predict_key))+" - "+predict_key
        else:
            predict_key = get_member_key_from_db(str(np.argmax(pred)))
            return str(get_member_name_from_db(predict_key))+" - "+predict_key
            # return str(list_dir[np.argmax(pred)])
