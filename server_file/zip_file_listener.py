import threading as th
import os
import zipfile

file_path_name = '/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/zipfiles/'

prev_file_list = []
prev_file_num = len(prev_file_list)


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


while True:
    cur_file_list = os.listdir(file_path_name)
    cur_file_list.sort()
    for i in range(len(cur_file_list)):
        try:
            if (len(cur_file_list[i]) != 32):
                file_name = file_path_name + cur_file_list[i]
                print(file_name)
                all_info_zip = zipfile.ZipFile(file_name)
                mkdir_p(file_path_name + cur_file_list[i][:-4])
                all_info_zip.extractall(file_name[:-4])
                all_info_zip.close()
                os.remove(file_name)
        except:
                print("File Error !")
                print(cur_file_list[i])
                break

