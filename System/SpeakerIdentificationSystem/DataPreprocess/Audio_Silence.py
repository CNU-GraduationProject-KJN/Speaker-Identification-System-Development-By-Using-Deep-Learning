from inaSpeechSegmenter import Segmenter
import librosa
import os
import numpy as np
from scipy import signal
from scipy.io import wavfile
from glob import iglob
from shutil import rmtree
from constants import *
import warnings
import errno


def mkdir_p(path):
    try:
        os.makedirs(path)
    except OSError as exc:
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else:
            raise


def removeNoEnergy(segmentation, audio, silenced_data_path):
    output_path = silenced_data_path +'/'+ audio.split('/')[-1]
    mkdir_p(silenced_data_path)
    # print("Audio File Name: {}".format(audio))

    samplerate, _ = wavfile.read(audio)
    y, sr = librosa.load(audio, sr=samplerate)
    # print("Audio Length: {}\n".format(len(y)))

    adjusted_y = list(y)
    for seg in segmentation:
        if seg[0] == 'noEnergy':
            # print('Delete Audio Segmentation : {}s ~ {}s'.format(int(seg[1]), int(seg[2])))
            del adjusted_y[int(seg[1] * sr):int(seg[2] * sr)]

    save_file(output_path, np.array(adjusted_y), sr)

def save_file(save_path, silenced_data, sr):
    # print("Output Audio Length: {}\n".format(len(adjusted_y)))
    # print("Output File Name: {}\n".format(save_path))
    librosa.output.write_wav(save_path, silenced_data, sr)

warnings.filterwarnings(action='ignore')
def voice_active_detecting(data_dir):
    origin_data_path = data_dir+'/origin'
    silenced_data_path = data_dir+'/preprocessed/wav'

    seg = Segmenter()
    for i, wav_filename in enumerate(iglob(os.path.join(origin_data_path, '**.wav'), recursive=True)):
        print(wav_filename)
        segmentation = seg(wav_filename)
        removeNoEnergy(segmentation, wav_filename, silenced_data_path)



