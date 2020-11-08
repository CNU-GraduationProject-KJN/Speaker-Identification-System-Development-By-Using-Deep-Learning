import librosa
import os
import nlpaug
import nlpaug.augmenter.audio as naa

FIRST_QUANTILE = 3.6
SECOND_QUANTILE = 4.6
THIRD_QUANTILE = 5.5

def speed_augmentation(data_dir):
    file_path = data_dir + '/preprocessed/wav'
    file_list = os.listdir(file_path)
    file_list.sort()

    for j, item in zip(range(len(file_list)), file_list):

        sel_file_name = file_path + "/" + item
        data, sr = librosa.load(sel_file_name, sr=8000, mono=True, duration=50)

        time = len(data) / sr

        if time < SECOND_QUANTILE:
            if time < FIRST_QUANTILE:
                aug = naa.SpeedAug(zone=(0, 1), factor=(0.73, 0.74))
                augmented_data = aug.augment(data)
            else:
                aug = naa.SpeedAug(zone=(0, 1), factor=(0.69, 0.71))
                augmented_data = aug.augment(data)
        else:
            if time < THIRD_QUANTILE:
                aug = naa.SpeedAug(zone=(0, 1), factor=(1.34, 1.4))
                augmented_data = aug.augment(data)
            else:
                aug = naa.SpeedAug(zone=(0, 1), factor=(1.48, 1.54))
                augmented_data = aug.augment(data)

        save_file(file_path, augmented_data, sr)


def save_file(save_path, augmented_data, sr):
    files = os.listdir(save_path)
    result_name = str(len(files) + 1)
    librosa.output.write_wav(save_path + "/" + result_name + ".wav", augmented_data, sr)
