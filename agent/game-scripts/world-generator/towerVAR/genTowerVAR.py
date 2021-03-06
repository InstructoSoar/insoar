import csv, re, string
import numpy as np
import sys
import matplotlib.pyplot as plt
import os
import glob, pprint
from random import randint

firstflag = 0
worldstatelist= ""
i=0

result = open("interpret-game-state-tower-change.soar", 'wb+')

for x in range(0,100):
        for infile in glob.glob('final-tower3v.world'):
                gFile= infile.replace('.world','') + str(x)
                print gFile
                didrandomremove = 0
                worldstatelist+= " " + gFile
                wm = open(infile, 'r')
                stats = wm.read(1000000);
                wm.close();
        
                objid = []
                predid = []
                
                
                #result = open(gFile + ".soar", 'wb+')
                result.write("\nsp {dialog-event*apply*game-state-change-respond*" + gFile + "\n");
                result.write("   (state <s> ^name dialog-event\n");
                result.write("              ^top-state <ts>\n");
                result.write("              ^operator <o>)\n");
                result.write("   (<o> ^name game-state-change-respond\n")
                result.write("        ^type " + gFile + ")\n");
                result.write("   (<ts> ^world <wo2>)\n")
                result.write("-->\n")
                result.write("   (<ts> ^world <wo2> -)\n")
                result.write("   (<ts> ^world <wo>)\n")
                result.write("   (<wo> ^objects <objs> ^predicates <preds> ^robot <ro>)\n")
                result.write("   (<ro> ^handle rosie ^item-type object ^arm.action wait ^predicate.handle rosie)\n")
                
                result.write("   (<objs> ^object <self>")
	
                for line in stats.splitlines():
                        if "objid" in line:
                                chunks = line.split()
                                objid.append("<o"+str(chunks[1]) + ">")
                                result.write(" <o" + str(chunks[1])+ ">")
                result.write(")\n")

                result.write("   (<self> ^type object ^handle self ^predicates.type object\n")
                #result.write("   (<preds> ^predicate")
                #add predicates
                if "predicate" in stats:
                        result.write(")\n   (<preds> ^predicate")
                        for line in stats.splitlines():
                                if "predicate " in line:
                                        chunks = line.split()
                                        predid.append(str(chunks[1]))
                                        result.write(" <" + str(chunks[1])+ ">")
                #add predicate sets
                for line in stats.splitlines():
                        if "predicate-set " in line:
                                chunks = line.split()
                                predid.append(str(chunks[1]))
                                result.write(" <" + str(chunks[1])+ ">")

                flag = 0
                index = -1
                index2 = -1

                predicates = ""
                for line in stats.splitlines():
                        if "objid" in line:
                                result.write(")\n")
                                index= index +1
                                result.write("(" + str(objid[index])+ " ^item-type object ^handle object-" + str(index) + " ^predicates <pr" + str(index) + ">)\n")
                                result.write("(<pr" + str(index) + "> ^visible true ")
                                flag = 1        
                        elif "predicate " in line:
                                if flag == 1:
                                        index=-1
                                result.write(")\n" + predicates)
                                predicates = ""
                                index= index +1
                                result.write("(<" + str(predid[index])+ "> ^item-type predicate ^handle " + str(predid[index]) )#+ " ^instance ")
                                flag = 2
                                firstflag = 1
                        elif "predicate-set " in line:
                                if flag == 1:
                                        index=-1
                                result.write(")\n" + predicates)
                                predicates = ""
                                index= index +1
                                result.write("(<" + str(predid[index])+ "> ^item-type predicate ^handle " + str(predid[index]) + " ^instance ")
                                flag = 3
                        elif line.strip():
                                if flag == 1:
                                        if (didrandomremove < 3 and randint(0,9) == 1):
                                                didrandomremove = didrandomremove + 1
                                                print "Removed " + line + " from " + str(objid[index])
                                        else:
                                                words = line.split()
                                                result.write("^" + str(words[0]) + " " + str(words[1]) + " ")
                                elif flag == 2:
                                        if (didrandomremove < 6 and randint(0,3) == 2):
                                                didrandomremove = didrandomremove + 1
                                                print "Removed " + line + " from " + str(predid[index])
                                        else:
                                                if (firstflag == 1):
                                                        firstflag = 2;
                                                        result.write(" ^instance ");
                                                index2 = index2 +1
                                                words = line.split()
                                                result.write("<ins" + str(index2) + "> ")
                                                predicates+= "(<ins" + str(index2) + "> ^1 <o" + str(words[0]) + "> ^2 <o" + str(words[1]) + ">)\n"
                                elif flag == 3:
                                        index2 = index2 +1
                                        result.write("<ins" + str(index2) + "> ")
                                        predicates+= "(<ins" + str(index2) + "> ^1 <set" + str(index2) + ">)\n(<set" + str(index2) + ">  ^object"
                                        for word in line.split():
                                                predicates+= " <o" +  str(word) + ">"
                                                predicates +=")\n"
                                                
                result.write(")\n" + predicates)            
                result.write("}\n")
                
                
result.close();
