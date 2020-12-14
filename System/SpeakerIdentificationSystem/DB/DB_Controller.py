import pymysql


def insert_value_into_db(idx, md5, name, path):
    conn = pymysql.connect(host='localhost', user='root', password='kjn', db='cnu_kjn_graduate', charset='utf8')
    try:
        with conn.cursor() as curs:
            sql = "insert into member_list(idx, member_key, name, path) value(" + idx + "," + md5 + "," + name + "," + path + ")"
            curs.execute(sql)
            rs = curs.fetchall()
        return rs
    finally:
        conn.close()


def get_member_list_from_db():
    conn = pymysql.connect(host='localhost', user='root', password='kjn', db='cnu_kjn_graduate', charset='utf8')
    try:
        with conn.cursor() as curs:
            sql = "select * from member_list"
            curs.execute(sql)
            rs = curs.fetchall()
        return rs
    finally:
        conn.close()


def get_last_member_path_from_db():
    conn = pymysql.connect(host='localhost', user='root', password='kjn', db='cnu_kjn_graduate', charset='utf8')
    try:
        with conn.cursor() as curs:
            sql = "select path from member_list order by idx desc limit 1"
            curs.execute(sql)
            rs = curs.fetchall()
            for row in rs:
                path = row[0]
        return path
    finally:
        conn.close()


def get_last_member_key_from_db():
    conn = pymysql.connect(host='localhost', user='root', password='kjn', db='cnu_kjn_graduate', charset='utf8')
    try:
        with conn.cursor() as curs:
            sql = "select member_key from member_list order by idx desc limit 1"
            curs.execute(sql)
            rs = curs.fetchall()
            for row in rs:
                key = row[0]
        return key
    finally:
        conn.close()


def get_member_count_from_db():
    conn = pymysql.connect(host='localhost', user='root', password='kjn', db='cnu_kjn_graduate', charset='utf8')
    try:
        with conn.cursor() as curs:
            sql = "select count(*) from member_list"
            curs.execute(sql)
            rs = curs.fetchall()
            for row in rs:
                count = row[0]
        return count
    finally:
        conn.close()


def get_member_idx_from_db(key):
    conn = pymysql.connect(host='localhost', user='root', password='kjn', db='cnu_kjn_graduate', charset='utf8')
    try:
        with conn.cursor() as curs:
            sql = "select idx from member_list where member_key=%s"
            curs.execute(sql, key)
            rs = curs.fetchall()
            for row in rs:
                idx = row[0]
        return idx
    finally:
        conn.close()


def get_member_key_from_db(idx):
    conn = pymysql.connect(host='localhost', user='root', password='kjn', db='cnu_kjn_graduate', charset='utf8')
    try:
        with conn.cursor() as curs:
            sql = "select member_key from member_list where idx=%s"
            curs.execute(sql, idx)
            rs = curs.fetchall()
            for row in rs:
                key = row[0]
        return key
    finally:
        conn.close()


def get_member_name_from_db(key):
    conn = pymysql.connect(host='localhost', user='root', password='kjn', db='cnu_kjn_graduate', charset='utf8')
    try:
        with conn.cursor() as curs:
            sql = "select name from member_list where member_key=%s"
            curs.execute(sql, key)
            rs = curs.fetchall()
            for row in rs:
                idx = row[0]
        return idx
    finally:
        conn.close()


def delete_member_from_db(key):
    conn = pymysql.connect(host='localhost', user='root', password='kjn', db='cnu_kjn_graduate', charset='utf8')
    try:
        with conn.cursor() as curs:
            idx = get_member_idx_from_db(key)
            sql = "delete from member_list where member_key = %s"
            curs.execute(sql, key)
            curs.fetchall()
            curs.execute("commit")

            count = get_member_count_from_db()
            if count > idx:
                key = get_last_member_key_from_db()
                sql = "update member_list set idx = "+str(idx)+" where member_key = %s"
                curs.execute(sql, key)
                curs.fetchall()
                curs.execute("commit")
                return True, key
            return False, key
    finally:
        conn.close()


def update_member_name_from_db(key, name):
    conn = pymysql.connect(host='localhost', user='root', password='kjn', db='cnu_kjn_graduate', charset='utf8')
    try:
        with conn.cursor() as curs:
            sql = "update member_list set name = " + name + " where member_key = %s"
            curs.execute(sql, key)
            curs.fetchall()
            curs.execute("commit")
    finally:
        conn.close()