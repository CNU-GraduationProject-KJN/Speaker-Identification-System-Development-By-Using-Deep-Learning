import os
import pickle
from glob import iglob
import numpy as np
import librosa
from constants import *
from shutil import rmtree

from DB.DB_Controller import get_member_idx_from_db

PAD_SIZE = 0
TARGET_SR = 8000
AUDIO_LENGTH = TARGET_SR * 3


def mkdir_p(path):
    import errno
    try:
        os.makedirs(path)
    except OSError as exc:
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else:
            raise


def del_folder(path):
    try:
        rmtree(path)
    except:
        pass


def read_audio_from_filename(filename, target_sr):
    audio, _ = librosa.load(filename, sr=TARGET_SR, mono=True)
    audio = audio.reshape(-1, 1)
    return audio


def convert_data(data_dir, key):
    class_id = get_member_idx_from_db(key)
    train_data_path = data_dir + '/preprocessed/pickle/train'
    test_data_path = data_dir + '/preprocessed/pickle/test'
    del_folder(train_data_path)
    del_folder(test_data_path)
    mkdir_p(train_data_path)
    mkdir_p(test_data_path)

    for i, wav_filename in enumerate(iglob(os.path.join(data_dir, 'preprocessed/wav/**.wav'), recursive=True)):

        audio_buf = read_audio_from_filename(wav_filename, target_sr=TARGET_SR)

        # normalize mean 0, variance 1
        audio_buf = (audio_buf - np.mean(audio_buf)) / np.std(audio_buf)
        original_length = len(audio_buf)
        # print(i, wav_filename, original_length, np.round(np.mean(audio_buf), 4), np.std(audio_buf))

        voice_seg = [audio_buf[PAD_SIZE: AUDIO_LENGTH + PAD_SIZE]]

        output_folder = train_data_path
        if wav_filename[-5:] == '5.wav' or wav_filename[-5:] == '8.wav':
            output_folder = test_data_path

        temp_split = wav_filename.replace('.', '/').split('/')
        output_filename = os.path.join(output_folder, str(temp_split[-2]) + '.pkl')
        for i_seg, audio_seg in enumerate(voice_seg):
            out = {'class_id': class_id,
                    'audio': audio_seg,
                    'sr': TARGET_SR}

            with open(str(output_filename), 'wb') as w:
                pickle.dump(out, w)
