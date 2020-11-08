import sys
import os
from API_Controller import delete_result
from DB.DB_Controller import delete_member_from_db
from shutil import rmtree
from DataPreprocess.wav_to_pickle import convert_train_data
from Speaker_Predictionor import model_training

key = sys.argv[1]
train_data_dir = '/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/unzipfiles'

def del_folder(path):
    try:
        rmtree(path)
    except:
        pass
def reset():
    print("reset")
    key_list = os.listdir(train_data_dir)
    model_training(train_data_dir, key_list)


print("delete :",key)
del_folder(train_data_dir + '/' + key)
update, update_key = delete_member_from_db(key)
if update:
    convert_train_data(train_data_dir + '/' + update_key, update_key)
delete_result(key, 'OK')
reset()
