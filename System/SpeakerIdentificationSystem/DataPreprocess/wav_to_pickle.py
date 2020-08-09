import os
import pickle
from glob import iglob
import numpy as np
import librosa
from constants import *
from shutil import rmtree

DATA_AUDIO_DIR = './train_data'
list_dir = os.listdir(DATA_AUDIO_DIR)
list_dir.sort()
class_ids = {list_dir[i]: i for i in range(len(list_dir))}

PAD_SIZE = 0
TARGET_SR = 8000
AUDIO_LENGTH = TARGET_SR * 10
SEGMENT_NUM = 1


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


#수정******************************************************
def extract_class_id(wav_filename):
    return class_ids.get(wav_filename.split('/')[2])


def read_audio_from_filename(filename, target_sr):
    audio, _ = librosa.load(filename, sr=TARGET_SR, mono=True)
    audio = audio.reshape(-1, 1)
    return audio


def convert_data(data_dir):
    train_data_path = data_dir+'/preprocessed/pickle/train'
    test_data_path = data_dir+'/preprocessed/pickle/test'
    del_folder(train_data_path)
    del_folder(test_data_path)
    mkdir_p(train_data_path)
    mkdir_p(test_data_path)

    for i, wav_filename in enumerate(iglob(os.path.join(data_dir, 'preprocessed/wav/**.wav'), recursive=True)):
        class_id = extract_class_id(wav_filename)
        audio_buf = read_audio_from_filename(wav_filename, target_sr=TARGET_SR)

        # normalize mean 0, variance 1
        audio_buf = (audio_buf - np.mean(audio_buf)) / np.std(audio_buf)
        original_length = len(audio_buf)
        # print(i, wav_filename, original_length, np.round(np.mean(audio_buf), 4), np.std(audio_buf))

        voice_seg = []
        voice_seg.append(audio_buf[PAD_SIZE: AUDIO_LENGTH + PAD_SIZE])

        output_folder = train_data_path
        if wav_filename[-5:] == '5.wav' or wav_filename[-5:] == '8.wav':
            output_folder = test_data_path


        temp_split = wav_filename.replace('.','/').split('/')

        output_filename = os.path.join(output_folder, str(temp_split[6])+ '.pkl')
        out_segs = []
        for i_seg, audio_seg in enumerate(voice_seg):
            out = {'class_id': class_id, # class_id DB에서 들고오기******************************************************
                   'audio': audio_seg,
                   'sr': TARGET_SR}

            with open(str(output_filename), 'wb') as w:
                pickle.dump(out, w)
