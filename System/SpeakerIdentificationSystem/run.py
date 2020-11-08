# Main
from API_Controller import identify_result, upload_result, modify_voice_result
from DataPreprocess.Audio_Preprocessor import AudioPreprocessor
from Speaker_Predictionor import model_training, model_testing
import os
from shutil import rmtree

train_data_dir = '/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/unzipfiles'
val_data_dir = '/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/val/unzipfiles'


def del_folder(path):
    try:
        rmtree(path)
    except:
        pass


def reset():
    print("reset")
    key_list = os.listdir(train_data_dir)
    model_training(train_data_dir, key_list)


def upload():
    print("upload")
    key = AudioPreprocessor(train_data_dir, True).run()
    upload_result(key, 'OK')
    reset()


def identify():
    print("identify")
    AudioPreprocessor(val_data_dir, False).run()
    key, name, status = model_testing()
    identify_result(key, name, status)
    del_folder(val_data_dir + '/' + os.listdir(val_data_dir)[0])


def modify_voice(key):
    AudioPreprocessor(train_data_dir, True).run()
    modify_voice_result(key, 'OK')
    reset()
