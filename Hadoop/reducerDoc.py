#!/usr/bin/env python
import sys
mywordcount={}
for line in sys.stdin:
	line=line.strip()
	word,count=line.split('\t',1)
	try:
		count=int(count)
	except ValueError:
		continue
	try:
		mywordcount[word]=mywordcount[word]+count
	except:
		mywordcount[word]=count
for word in mywordcount.keys():
	print("%s" %(word),mywordcount[word])

