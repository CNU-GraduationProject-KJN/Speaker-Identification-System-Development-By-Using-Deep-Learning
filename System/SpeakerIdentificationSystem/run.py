# Main
from DB.db_controller import delete_member_from_db, update_member_name_from_db
from DataPreprocess.audio_preprocessor import AudioPreprocessor
from DataPreprocess.wav_to_pickle import convert_data
from speaker_predictionor import model_training, model_testing
import os
from shutil import rmtree

data_dir = '/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/unzipfiles'


def del_folder(path):
    try:
        rmtree(path)
    except:
        pass


def reset():
    print("reset")
    key_list = os.listdir(data_dir)
    model_training(data_dir, key_list)


def upload():
    print("upload")
    AudioPreprocessor(data_dir, True).run()
    reset()


def identify():
    print("identify")
    AudioPreprocessor(data_dir, False).run()
    model_testing()


def remove(key):
    print("remove")
    del_folder(data_dir + '/' + key)
    update_key = delete_member_from_db(key)
    convert_data(data_dir + '/' + update_key, update_key)
    reset()


def modify_name(key, name):
    update_member_name_from_db(key, name)


def modify_audio(key):
    upload()


def main():
    # while True:
    print("------menu------")
    print("1.신원등록\n2.신원확인\n3.종료")
    choice = int(input())

    if choice == 1:
        print('identity_register -- 어플리케이션으로 할 예정')
        upload()
        # 음성원본 저장 폴더 반환 -> data_dir
        # 후 데이터 전처리
        # 훈련
        # AudioPreprocessor(data_dir, True).run()
        # model_training()

    elif choice == 2:
        print('identity_lookup -- 어플리케이션으로 할 예정')
        # 음성원본 받음 wav파일
        print(model_testing())
        # identity_modify
        # identity_remove
    elif choice == 3:
        # remove('45a3cd1e53db5a9d508f38f77a6f34e2')
        # print('시스템을 종료합니다.')
        return None
    else:
        print('\n잘못된 입력입니다.\n')


if __name__ == '__main__':
    main()
