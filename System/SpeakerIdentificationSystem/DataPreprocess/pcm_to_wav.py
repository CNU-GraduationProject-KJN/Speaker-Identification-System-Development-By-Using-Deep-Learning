import os
import wave
from shutil import rmtree

from DB.db_controller import get_last_member_key_and_path_from_db


def pcm2wav(pcm_file, wav_file, channels=1, bit_depth=16, sampling_rate=16000):
    # Check if the options are valid.
    if bit_depth % 8 != 0:
        raise ValueError("bit_depth " + str(bit_depth) + " must be a multiple of 8.")

    # Read the .pcm file as a binary file and store the data to pcm_data
    with open(pcm_file, 'rb') as opened_pcm_file:
        pcm_data = opened_pcm_file.read()

        obj2write = wave.open(wav_file, 'wb')
        obj2write.setnchannels(channels)
        obj2write.setsampwidth(bit_depth // 8)
        obj2write.setframerate(sampling_rate)
        obj2write.writeframes(pcm_data)
        obj2write.close()


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


def convert_train_file():
    rs = get_last_member_key_and_path_from_db()
    file_origin_path = rs[0][1] + "/origin/"
    key = rs[0][0]

    mkdir_p(file_origin_path)
    file_list = os.listdir(file_origin_path)
    for file in file_list:
        if file[-4:] == '.pcm':
            pcm2wav(file_origin_path + file, file_origin_path + file[0] + '.wav', 1, 16, 16000)
            os.remove(file_origin_path + file)

    return key


def convert_test_file():
    data_dir = '/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/val'
    key = os.listdir(data_dir)[0]
    path = data_dir+'/'+key

    mkdir_p(path)
    file_list = os.listdir(path+ "/origin/")
    for file in file_list:
        if file[-4:] == '.pcm':
            pcm2wav(path + file, path + file[0] + '.wav', 1, 16, 16000)
            os.remove(path + file)

    return key



