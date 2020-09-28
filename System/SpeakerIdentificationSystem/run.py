# Main
from DataPreprocess.audio_preprocessor import AudioPreprocessor
from speaker_predictionor import model_training, model_testing


def upload():
    print("upload")
    data_dir = '/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/unzipfiles'
    path, key_list = AudioPreprocessor(data_dir, True).run()
    model_training(path, key_list)

def identify():
   print("identify")
   data_dir = '/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/unzipfiles'
   AudioPreprocessor(data_dir, False).run()
   model_testing()


def main():
    # while True:
        print("------menu------")
        print("1.신원등록\n2.신원확인\n3.종료")
        choice = int(input())
        data_dir = './train_data/2017019740029_limdonggeun'
        
        if choice == 1:
            print('identity_register -- 어플리케이션으로 할 예정')
            #음성원본 저장 폴더 반환 -> data_dir
            #후 데이터 전처리
            #훈련
            AudioPreprocessor(data_dir, True).run()
            model_training()

        elif choice == 2:
            print('identity_lookup -- 어플리케이션으로 할 예정')
            # 음성원본 받음 wav파일
            AudioPreprocessor(data_dir, False).run()

            # identity_modify
            # identity_remove
        elif choice ==3:
            print('시스템을 종료합니다.')
            return None
        else:
            print('\n잘못된 입력입니다.\n')

if __name__ == '__main__':
    main()
