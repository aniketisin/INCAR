import math;
import string;
import re;
import sys ;
import os
from math import sqrt;
from math import acos;
def Cluster(filename):
        #Create a Dictionary !!  
        d = {} ;
        f = open( filename,'r' ) ;
	f1=open('~/Downloads/mincoor/' + filename + '.hyd', 'w+')
        try:
                for line in f:
#		Split the line into a list 
                        l=[]
                        l.extend(line[0:5].split());
                        l.extend(line[5:12].split());
                        l.extend(line[ 13:17].split());
                        l.extend(line[ 17:21].split());
                        l.extend(line[ 21:22].split());
                        l.extend(line[ 22:26].split());
			l.extend(line[ 30:38].split());
			l.extend(line[ 38:46].split());
			l.extend(line[ 46:54].split());
#                       print l 

		        #Split the line automatically by white space charaters like Tab , Space etc. 
       			 #print l Uncomment the line to see the output .. !! 
                        key = l[5]+" "+l[4]+" "+l[3] ;

		       	 #Do not change the ordering of first three columns in coordinate file .. They define the key.
		        #print key

                        if ( key in d ):
                                d[key][l[2]] = l[6]+" "+ l[7]+" "+l[8];
#                               print "\t";
                        else:
                                d[key] = {} ;
                                d[key][l[2]] = l[6]+" "+l[7]+" "+l[8];
#                               print "\t"
        finally:
                                return d;
#                               print "\t";
        f.close()


dict1 = Cluster( sys.argv[1] ) ;
def bif(filename):
	FileOpen = open(filename);
	Space = re.compile(r'\s+');
	b1 = {}
	for Lines in FileOpen:
		Lines = Space.split(Lines[:-1]);
#		print Lines;
#		if len(Lines) > 10:
		try:
#			if (len(Lines) > 10):
			if (len(Lines) > 10  and Lines[3] != "HOH" and ( len(Lines[2]) ==  2  or Lines[2][2] != "'" or Lines[2] == "O2'" ) and ( len(Lines[2])== 2 or Lines[7][2] != "'"  or Lines[7] =="O2'" )) and Lines[8] != "HOH":
				b1[Lines[1]+" "+Lines[11]+" "+Lines[6]] = Lines[4]+Lines[5]+" "+Lines[9]+Lines[10]+" "+Lines[2]+Lines[3]+" "+Lines[11]+" "+Lines[7]+Lines[8]
		except:
			"DO NOTHING"
	x = b1.keys();
	pairs = []
	triples = []
	outstr = ""
	for i in xrange(len(x)):
		z=x[i].split(" ");
	#	print b1[z]
		for j in xrange(i+1,len(x)-1):
			y=x[j].split(" ");
			pb1 = b1[z[0]+" "+z[1]+" "+z[2]].split(" ")
			qb1 = b1[y[0]+" "+y[1]+" "+y[2]].split(" ")
#			38X 60X N2G H21 O2C 259X 50X OH2HOH H1O O4'C
			if z[0] == y[0] and z[1] != y[1] and z[2] == y[2] :
                                outstr = "D--HH-A"+" "+pb1[0]+" "+ pb1[1]+" "+pb1[2]+" "+"<"+" "+pb1[3]+" "+qb1[3]+" "+ pb1[4]
                                pairs.append(outstr)
                                outstr = ""
                        elif z[0] == y[0] and z[1] == y[1] and z[2] != y[2]:
                                if pb1[1] == qb1[1]:
                #                       print "Bifurcated pairs"
                                        outstr= "D--H-AA"+" "+pb1[0]+" "+pb1[1]+" "+pb1[2]+" "+"-"+" "+pb1[3]+" "+"<"+" "+pb1[4]+" "+qb1[4]
                                        pairs.append(outstr)
                                        outstr = ""
                                else:
                #                       print "Bifurcated triple"
                                        outstr= "D--H-AA"+" "+pb1[0]+" "+pb1[1]+" "+"&"+" "+qb1[1]+" "+pb1[2]+" "+"-"+" "+pb1[3]+" "+"<"+" "+pb1[4]+" "+qb1[4]
                                        triples.append(outstr)
                                        outstr = ""
                        elif z[0] != y[0] and z[1] != y[1] and z[2] == y[2]:
                                if pb1[0] == qb1[0]:
                #                       print "Bifurcated pairs"
                                        outstr = "DD-HH-A"+" "+pb1[0]+" "+pb1[1]+" "+pb1[2]+" "+qb1[2]+" "+"-"+" "+pb1[3]+" "+qb1[3]+" "+">"+" "+pb1[4]
                                        pairs.append(outstr)
                                        outstr = ""
                                else:
                #                       print "Bifurcated triple"
                                        outstr= "DD-HH-A"+" "+pb1[0]+" "+"&"+" "+qb1[0]+" "+pb1[1]+" "+pb1[2]+" "+qb1[2]+" "+"-"+" "+pb1[3]+" "+qb1[3]+" "+">"+" "+pb1[4]
                                        triples.append(outstr)
                                        outstr = ""
                        elif z[0] == y[0] and z[1] != y[1] and z[2] != y[2]:
                                if pb1[1] == qb1[1]:
                #                       print "Bifurcated pairs"
                                        outstr = "D-HH-AA"+" "+pb1[0]+" "+pb1[1]+" "+pb1[2]+" "+"<"+" "+pb1[3]+" "+qb1[3]+" "+">"+" "+pb1[4]+" "+qb1[4]
                                        pairs.append(outstr)
                                        outstr = ""
                                else:
                #                       print "Bifuracted triples"
                                        outstr = "D-HH-AA"+" "+ pb1[0]+" "+pb1[1]+" "+"&"+" "+qb1[1]+" "+pb1[2]+" "+"<"+" "+pb1[3]+" "+qb1[3]+" "+">"+" "+pb1[4]+" "+qb1[4]
                                        triples.append(outstr)
                                        outstr = ""
			elif z[0] != y[0] and z[1] != y[1] and (z[2] == y[0]):
                                if pb1[0] == qb1[1]:
                #                       print "Bifurcated pairs"
                                        outstr = "D-H-A-D"+" "+pb1[0]+" "+pb1[1]+" "+" "+pb1[2]+" "+pb1[3]+" "+pb1[4]+" "+qb1[3]+" "+qb1[4]
                                        pairs.append(outstr)
                                        outstr = ""
                                else:
                #                       print "Bifurcated triples"
                                        outstr = "D-H-A-D"+" "+pb1[0]+" "+pb1[1]+" "+qb1[1]+" "+pb1[2]+" "+pb1[3]+" "+pb1[4]+" "+qb1[3]+" "+qb1[4]
                                        triples.append(outstr)
                                        outstr = ""

	print "Bifurcatedpairs \t"
	f1.write("Bifurcatedpairs \t")
	for i in pairs:
		f1.write(i)
		print i;
	f1.write("Bifurcated triples \t")
	print "Bifurcated triples \t"
	for i in triples:
		f1.write(i)
		print i;
#	print b1

	FileOpen.close()
dict3 = bif(sys.argv[2]);		
def water(filename):
	FileOpen = open(filename);
	Space = re.compile(r'\s+');
	h1= {}
	h2 ={}
	water = []
	for Lines in FileOpen:
		Lines = Space.split(Lines[:-1]);
		if( len(Lines) >= 10 and (Lines[15] == "W-b" or (Lines[15] == 's-W' and Lines[2] == "O2'") or(Lines[15] == 'W-s' and Lines[7] == "O2'") or Lines[15] == "b-W")):
        	       	if Lines[8] == "HOH":
                	       	h1[Lines[3]+" " +Lines[4]+" "+Lines[5]] = Lines[8]+" " +Lines[9]+" "+Lines[10]
	                if Lines[3] == "HOH":
        	       	        h2[Lines[3]+" " +Lines[4]+" "+Lines[5]] = Lines[8]+" " +Lines[9]+" "+Lines[10]
	print "Water mediated base pairs	"
	f1.write("Water mediated base pairs	")
	for i in h1:
		for j in h2:
			if h1[i] == j and i != h2[j]:
				outstr = i+" "+h1[i]+" "+h2[j]
				water.append(outstr)
	w1={}
	for j in water:
		w =j.split();
		if int(w[1]) < int(w[7]):
			w1[w[1]+w[2]+" "+w[7]+w[8]]=w[3]+" "+w[4]+w[5]
#			print w[1]+w[2]+" "+w[7]+w[8]+" "+w[0]+":"+w[6]+" "+w[3]+" "+w[4]+w[5]
		else:
			w1[ w[7]+w[8]+" "+w[1]+w[2]] =w[3]+" "+w[4]+w[5]
#			print w[7]+w[8]+" "+w[1]+w[2]+" "+w[6]+":"+w[0]+" "+w[3]+" "+w[4]+w[5]
	outstr = ""	
	for i in water:
		f1.write(i)
		print i;
	return w1
	FileOpen.close()
dict2 = water( sys.argv[2]);
class Extracter:
	def __init__(Self):

		Self.OutputDict = {};
		Self.dict2 = {};
		Self.dct = {};
		Self.h1 = {};
		Self.h2 = {};
		Self.lst = [];
		Self.lst1 = [];
		Self.lst2 = [];
		Self.bps = [];
		return ;
	def ParseFile(Self,InputFileName):
		FileOpen = open(InputFileName);
		
		Space = re.compile(r'\s+');

		for Lines in FileOpen:
			Lines = Space.split(Lines[:-1]);
			
			if(len(Lines) >=10 and Lines[0] != 'Bond' and Lines[0] != 'H-Bond'):

				Key    = (Lines[4]+" " +Lines[5]+" "+Lines[3],Lines[9]+" "+Lines[10]+" "+Lines[8]);
				OppKey = (Lines[9]+" " +Lines[10]+" "+Lines[8],Lines[4]+" "+Lines[5]+" "+Lines[3]);
		#		print "=>"+Key;

				if(Self.OutputDict.has_key(Key)):
					Self.OutputDict[Key].append(Lines);

				elif(Self.OutputDict.has_key(OppKey)):
					Self.OutputDict[OppKey].append(Lines);

				else:
					Self.OutputDict[Key] = [];
					Self.OutputDict[Key].append(Lines);
#		print Self.OutputDict
#		for i in Self.OutputDict:
#			print i,	
#			temp = Self.OutputDict[i]
#			for j in range(0,len(Self.OutputDict[i])):
#				print temp[j][0],temp[j][2],
#			print
		outName = sys.argv[2] + ".all"; # Final output file => <inputfile>.out
		fout = open(outName,"w");
		outstr = "================================================================================================\n"
		outstr += "R.No1--R.No2\t RT1--RT2\t\t\t ALL INTER-RESIDUE CONTACTS \n";
		outstr += "================================================================================================"
		fout.write(outstr);
		fout.write("\n");

		for Key in sorted(Self.OutputDict.iterkeys(), lambda x, y: 1 if int(x[0].split(" ")[0]) > int(y[0].split(" ")[0]) else -1):
			outstr = ""
			str1 = ""
			b = Key[0]
                        c = Key[1]
#			outstr += b.split()[0]+b.split()[1]+" "+b.split()[2]+"---"+c.split()[0]+c.split()[1]+" "+c.split()[2]+ "\t";
			outstr += b.split()[0]+b.split()[1]+"----"+c.split()[0]+c.split()[1]+"\t "+b.split()[2]+"------"+c.split()[2]; 
			temp = Self.OutputDict[Key]
                        for j in range(0,len(Self.OutputDict[Key])):
                                outstr +=  "\t"+"   "+temp[j][2]+temp[j][3]+"--"+temp[j][7]+temp[j][8];
                        fout.write(outstr);
                        fout.write("\n");
			if( (b[-1] == 'A' or b[-1] == 'G' )and b[-2] == " " ):
				temp=dict1[b]
#				print temp;
				x = 0.0
				y = 0.0
				z = 0.0
				for j in temp:
			 		if j == 'C4' or j == 'C5' or j == 'N7' or j == 'C8'or j == 'N9' :
			#	if j == 'N9' :
		                                t = temp[j].split()
                		                x = x + float(t[0])
                               			y = y + float(t[1])
                             			z = z + float(t[2])
					x2=round(x/5,3)
					y2=round(y/5,3)
					z2=round(z/5,3)
					c1=dict1[b]["C1'"].rsplit();
			#	print x2,y2,z2,c1 ;

			elif( (b[-1] == 'C' or b[-1] == 'U' )and b[-2] == " "):
                                temp=dict1[b]
#				print temp;

                                x = 0.0
                                y = 0.0
                                z = 0.0
                                for j in temp:
					if j == 'N1' or j == 'C2' or j == 'N3' or j == 'C4'or j == 'C5' or j == 'C6':
				#	if j == 'N1' :
                                                t = temp[j].split()
                                                x = x + float(t[0])
                                                y = y + float(t[1])
                                                z = z + float(t[2])
                                	x2=round(x/6,3)
					y2=round(y/6,3)
					z2=round(z/6,3)

					c1=dict1[b]["C1'"].rsplit();
		 	#	print x2,y2,z2,c1 ;

#******** Defining x3,y3,z3********
			try :
				x1 =float( c1[0] ) ;
				y1 =float( c1[1] ) ;
				z1 =float( c1[2] ) ;
			except :
				"do nthing"


#**********Ends*****************
			if( (c[-1] == 'C' or c[-1] == 'U')and c[-2] == " "):
                                temp1= dict1[c]
#				print temp;

                                x=0.0
                                y=0.0
                                z=0.0
                                for j in temp1:
					if j == 'N1' or j == 'C2' or j == 'N3' or j == 'C4'or j == 'C5' or j == 'C6':
#					if j == 'N1':
                                                t = temp1[j].split()
                                                x = x + float(t[0])
                                                y = y + float(t[1])
                                                z = z + float(t[2])
                                	x3=round(x/6,3)
					y3=round(y/6,3)
					z3=round(z/6,3)
					c2=dict1[c]["C1'"].rsplit();

			#	print x3,y3, z3 , c2 ;

			elif( (c[-1] == 'G' or c[-1] == 'A' ) and c[-2] == " "):
				temp1= dict1[c]
#				print temp;

				x=0.0
				y=0.0
				z=0.0
				for j in temp1:
					if j == 'C4' or j == 'C5' or j == 'N7' or j == 'C8'or j == 'N9':
#					if j == 'N9':
                	        	        t = temp1[j].split()
                               			x = x + float(t[0])
                               			y = y + float(t[1])
                               			z = z + float(t[2])
				#		print x;
	               		#	print x;
					x3=round(x/5,3)
					y3=round(y/5,3)
					z3=round(z/5,3)
					c2=dict1[c]["C1'"].rsplit();
			#	print x3,y3,z3,c2 ;
			#******** Defining x4,y4,z4
			try:
				x4 = float( c2[0] ) ;
				y4 = float( c2[1] ) ;
				z4 = float( c2[2] ) ;
			
				
		      # ******************************Torsion Calculation**************************
				a1=x2-x1 ; 
				a2=y2-y1 ; 
				a3=z2-z1 ;
				b1=x3-x2 ;
				b2=y3-y2 ;
				b3=z3-z2;
				c1=x4-x3 ;
				c2=y4-y3 ;
				c3=z4-z3;

	
				a4 = sqrt((a1*a1)+(a2*a2)+(a3*a3));
				b4 = sqrt((b1*b1)+(b2*b2)+(b3*b3));
				c4 = sqrt((c1*c1)+(c2*c2)+(c3*c3));
		
				A=((a2*b3)-(a3*b2));
				A=A/(a4*b4);
				B=((a1*b3)-(a3*b1))/(a4*b4);
				C=((a1*b2)-(a2*b1))/(a4*b4);

				X=((b2*c3)-(b3*c2))/(b4*c4);
				Y=((b1*c3)-(b3*c1))/(b4*c4);
				Z=((b1*c2)-(b2*c1))/(b4*c4);					
		
				p=(A*X)+(B*Y)+(C*Z);
		
				q=(sqrt((A*A)+(B*B)+(C*C)))*(sqrt((X*X) + (Y*Y) + (Z*Z)));
				ang=acos(p/q);
				ang=(ang*7*180)/22;  
			# Converting the angle into degrees	
#			        print( "Torsion angle is " + str(ang)) ;
	
				Type='';
				if( ang > 90 ):
					Type = 'Trans';
				else:
					Type = 'Cis' ;
				ang = str( ang ) ;
			except:
                                "Enjoy"
			
			for i in (Self.OutputDict[Key]):
			#	print Key;
                                k1=Key[0].split(" ")
                               	k2=Key[1].split(" ")
				a=str(i[16]).split(":")
				if(i[15] == "b-b" or (i[15] == 's-b' and i[2] == "O2'") or (i[15] == 's-s' and i[2] == "O2'" and i[7] == "O2'") or (i[15] == 'b-s' and i[7] == "O2'") ):
			#	if(i[-1] != "" and i[-2] == "b-b"):
			#	if(i[14] == "b-b"):
					i[16]=i[16]+" "+Type;
					Self.dct[str(i[2])+str(i[3])+"-"+str(i[7])+str(i[8])]=str(i[4])+str(i[5])+" "+str(i[9])+str(i[10])+" "+a[0][1]+":"+a[1][1]+" "+a[0][0]+":"+a[1][0]+" "+Type
			#	print k1[0]+k1[1]+" "+k2[0]+k2[1]+" "+k1[2]+" "+k2[2]+" "+str(i[2])+"("+ str(i[3]) +")"+"--"+str(i[7])+"("+ str(i[8]) +")"+" "+str(i[-2])+" "+str(i[-1]) #uncomment it for full info
	
			Self.lst1 = Self.dct.keys();
      		     	l=len(Self.dct.keys());		
			flag = 0;
#			bps=[]
			outstr = ""
			for i in Self.dct:
		#		print i
				if i[0] == "N" and i[4] == "N" and i[5] != "2":
					outstr =  str(Self.dct[i])+" "+str(l)
					Self.bps.append(outstr);
					break;
				else:
					flag=flag+1;
			if flag == l:
				flag = 0;
 				for i in Self.dct:					
					if i[0] == "C" and i[1] != "2":
						outstr =  str(Self.dct[i])+" "+str(l)
						Self.bps.append(outstr);
	                                	break;
					else:
                                        	flag=flag+1;
			if flag == l:
                                flag = 0;
                                for i in Self.dct:
					if i[0] == "N" and i[4] == "O" and i[6] == "'":
						outstr =  str(Self.dct[i])+" "+str(l)
						Self.bps.append(outstr);
                               			break;
					else:
                                                flag=flag+1;
			if flag == l:
                                flag = 0;
                                for i in Self.dct:
					if i[0] == "O" and i[2] == "'" and i[5] == "O" and i[7] == "'":
						outstr =  str(Self.dct[i])+" "+str(l)
						Self.bps.append(outstr);
                	           	  	break;
					else:
                                                flag=flag+1;
			if flag == l:
                                flag = 0;
                                for i in Self.dct:	
					if i[0] == "O" and i[2] == "'" and i[5] == "N":
						outstr =  str(Self.dct[i])+" "+str(l)
						Self.bps.append(outstr);
                	              		break;
					else:
                                                flag=flag+1;
			if flag == l:
                                flag = 0;
                                for i in Self.dct:    
					if i[4] == "O" and i[6] != "'":
						outstr =  str(Self.dct[i])+" "+str(l)
						Self.bps.append(outstr);
                	             	        break;
					else:
                                                flag=flag+1;
			if flag == l:
                                flag = 0;
                                for i in Self.dct:
                                        if i[0] == "N" and i[4] == "O":
						outstr =  str(Self.dct[i])+" "+str(l)
						Self.bps.append(outstr);
                                                break;
                                        else:
                                                flag=flag+1;
			Self.dct = {};
#			print Self.bps
			outstr = "";    
			flag = 0;
			#for i in Self.lst:
			#	print i;
			Self.lst1 = []; 
			Self.dct = {}; 
		print "Base-pairs in", sys.argv[2]
		f1.write("Base-pairs in" + str(sys.argv[2]))		
		for i in Self.bps:
			op = i.split(" ")
			lop = len(op)
#			print op, op[0],op[1]
			if int(op[0][0:-1]) < int(op[1][0:-1]):
				try:
					f1.write(i,dict2[op[0]+" "+op[1]],"*")
					print i,dict2[op[0]+" "+op[1]],"*"
				except: 
					"kjjsd",
			else:
				try:
					f1.write(op[1]+" "+op[0]+" "+op[2][2]+":"+op[2][0]+" "+op[3][2]+":"+op[3][0] +" "+op[4]+" "+op[5], dict2[op[1]+" "+op[0]],"*")
	                                print op[1]+" "+op[0]+" "+op[2][2]+":"+op[2][0]+" "+op[3][2]+":"+op[3][0] +" "+op[4]+" "+op[5], dict2[op[1]+" "+op[0]],"*"
				except:
					"scwew",
			f1.write("\n")		
			print "\n"
		fout.write("\n");
		fout.close();	
#			dct = Self.OutputDict.keys();
#			print dct['G N 47', 'A N 104'];
#			for i,j in dct :
#				for k in dct[i,j]:
#					for m in k :
#						print m+"\t",
#					print  
		
Extracter = Extracter();
Extracter.ParseFile(sys.argv[2]);
