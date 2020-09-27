import threading as th
import os
import zipfile
import sys
from shutil import rmtree
from run import upload
from run import identify
#from run import reupload

zipfile_name = sys.argv[1]
flag = sys.argv[2]

print(zipfile_name, flag)

dir_name = '/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/'
zipfile_path_name = dir_name + 'zipfiles/'
unzipfile_path_name = dir_name + 'unzipfiles/'

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
    except OSError:
        pass

le True:
    upload_filename = zipfile_path_name + zipfile_name
    batch_filename = unzipfile_path_name + zipfile_name

    try:
        print(batch_filename)
        all_info_zip = zipfile.ZipFile(upload_filename + ".zip")

        mkdir_p(batch_filename + "/origin")
        all_info_zip.extractall(batch_filename + "/origin")
        all_info_zip.close()

        os.remove(upload_filename + ".zip")
        break
    except Exception as ex:
        print("File Error !", ex)
        print(upload_filename)
        pass

if int(flag) == 1:
    print("upload function call")
    upload()
elif int(flag) == 2:
    print("check user")
    identify()
#else :
#    reupload(zipfile_name)

