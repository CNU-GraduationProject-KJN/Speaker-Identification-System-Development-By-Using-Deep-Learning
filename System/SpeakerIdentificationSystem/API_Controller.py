import requests
import json

url = '168.188.126.212/'


def upload_result(key, result):
    result_url = url+'/upload/result/'+key
    newItem = {
            "id": key,
            "result": result
    }
    requests.post(result_url, data=newItem)


def delete_result(key, result):
    result_url = url+'/delete/result/'+key
    newItem = {
            "id": key,
            "result": result
    }
    requests.post(result_url, data=newItem)


def modify_name_result(key, result):
    result_url = url+'/modifyName/result/'+key
    newItem = {
            "id": key,
            "result": result
    }
    requests.post(result_url, data=newItem)


def modify_voice_result(key, result):
    result_url = url+'/modifyVoice/result/'+key
    newItem = {
            "id": key,
            "result": result
    }
    requests.post(result_url, data=newItem)


def identify_result(key, name, result):
    result_url = url+'/identify/result/'+key
    newItem = {
            "id": key,
            "name": name,
            "result": result
    }
    requests.post(result_url, data=newItem)


