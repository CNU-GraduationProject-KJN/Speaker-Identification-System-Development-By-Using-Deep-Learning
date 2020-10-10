import os
from pathlib import Path

from DataPreprocess.Audio_Silence import voice_active_detecting
from DataPreprocess.Audio_Augmentation import speed_augmentation
from DataPreprocess.pcm_to_wav import convert_train_file, convert_test_file
from DataPreprocess.wav_to_pickle import convert_data


class AudioPreprocessor(object):
    def __init__(self, data_dir, train):
        self.data_dir = data_dir
        self.train = train

    def run(self):
        print("run")
        if self.train:
            key = convert_train_file()
        else:
            key = convert_test_file()
        # key='fsjdkfjsldk'
        path = self.data_dir+'/'+key

        voice_active_detecting(path)
        if self.train:
            speed_augmentation(path)
        convert_data(path, key)
        return key
