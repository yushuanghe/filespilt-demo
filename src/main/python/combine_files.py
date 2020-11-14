# -*- coding:utf-8*-

import os
import os.path
import time
time1 = time.time()
##########################合并同一个文件夹下多个txt################


def MergeTxt(filepath, exclude_file, outfile):
    k = open(filepath+outfile, 'a+', encoding='utf8',)
    for parent, dirnames, filenames in os.walk(filepath):
        for filepath in filenames:
            if filepath == exclude_file:
                continue

            print(filepath)
            txtPath = os.path.join(parent, filepath)  # txtpath就是所有文件夹的路径
            f = open(txtPath, encoding='utf8')
            ##########换行写入##################
            k.write(f.read()+"\n")
    k.close()
    print("搞定")


if __name__ == '__main__':
    filepath = 'C:/Users/yushu/Downloads/file_split/'
    exclude_file = 'zhifubao_request_20201113.txt'
    outfile = 'result.txt'
    MergeTxt(filepath, exclude_file, outfile)
    time2 = time.time()
    print(u'总共耗时：' + str(time2 - time1) + 's')
