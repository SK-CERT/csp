#!/usr/bin/env python3
# coding=utf-8
"""
    Tools to find duplicate in galaxies
"""
import json
import os
import collections


def loadjsons(path):
    """
      Find all Jsons and load them in a dict
    """
    files = []
    data = []
    for name in os.listdir(path):
        if os.path.isfile(os.path.join(path, name)) and name.endswith('.json'):
            files.append(name)
    for jfile in files:
        data.append(json.load(open("%s/%s" % (path, jfile))))
    return data

if __name__ == '__main__':
    """
        Iterate all name + synonyms
        tell what is duplicated.
    """
    jsons = loadjsons("../clusters")
    counter = collections.Counter()
    namespace = []
    for djson in jsons:
        items = djson.get('values')
        for entry in items:
            name = entry.get('value').strip().lower()
            counter[name]+=1
            namespace.append([name, djson.get('name')])
            try:
                for synonym in entry.get('meta').get('synonyms'):
                    name = synonym.strip().lower()
                    counter[name]+=1
                    namespace.append([name, djson.get('name')])
            except (AttributeError, TypeError):
                pass
    counter = dict(counter)
    for key, val in counter.items():
        if val>1:
            print ("Warning duplicate %s" % key)
            for item in namespace:
                if item[0]==key:
                    print (item)
