import os
from pathlib import Path

from DataPreprocess.audio_silence import voice_active_detecting
from DataPreprocess.audio_augmentation import speed_augmentation
from DataPreprocess.wav_to_pickle import convert_data


class AudioPreprocessor(object):
    def __init__(self, data_dir, train):
        self.data_dir = data_dir
        self.train = train


    def run(self):

        voice_active_detecting(self.data_dir)
        if self.train: speed_augmentation(self.data_dir)
        convert_data(self.data_dir)



