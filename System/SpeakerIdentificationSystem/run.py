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


def main():
    # while True:
    print("------menu------")
    print("1.신원등록\n2.신원확인\n3.종료")
    choice = int(input())

    if choice == 1:
        print('identity_register -- 어플리케이션으로 할 예정')
        reset()
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
        # delete('76797b53f93d94679c6a39a94e4779a6')
        # print('시스템을 종료합니다.')
        return None
    else:
        print('\n잘못된 입력입니다.\n')


if __name__ == '__main__':
    main()
