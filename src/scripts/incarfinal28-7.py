#!/usr/bin/python

import sys;
import string;
from math import sqrt;
from math import acos;

loc = sys.argv[2] + "../src/scripts/"

#	Considers O of HOH , O5' and O3' in the code itself.

dictD_atoms={};
dictA_atoms={};

baseAtoms = [];
sugarAtoms = [];
backBoneAtoms = [];

WC={};
H={};
S={};
adj={};
dDA = [];
dHA = [];
DHA = [];

# The input criteria file consists of the criteria for finding the bond. Like, the Donor-Acceptor Bond distance (dDA) and Bond angle (DHA) etc..
# It also contains residue specific base atoms for finding the sub-type of bond formed between bases. like DA/AA => Donor Adenine / Acceptor Adenine.
# Similarly the list of base atoms, backbone atoms etc.. and also atoms specific to them SA => Sugar Adenine atoms

fin = open(loc+"cuminp.txt","r");
ln = fin.readline();
while ln != "":
	line = ln.split('\n')[0];
	if "dDA" in line:
		dDA = line.split("=")[1].split("-");
	elif "dHA" in line:
		dHA = line.split("=")[1].split("-");
	elif "DHA" in line:
		DHA = line.split("=")[1].split("-");
	elif "DA" in line:
		dictD_atoms["A"] = line.split("=")[1].split(",");
	elif "DG" in line:
		dictD_atoms["G"] = line.split("=")[1].split(",");
	elif "DC" in line:
		dictD_atoms["C"] = line.split("=")[1].split(",");
	elif "DU" in line:
		dictD_atoms["U"] = line.split("=")[1].split(",");
	elif "DO" in line:
		dictD_atoms["O"] = line.split("=")[1].split(",");
	elif "AA" in line:
		dictA_atoms["A"] = line.split("=")[1].split(",");
	elif "AG" in line:
		dictA_atoms["G"] = line.split("=")[1].split(",");
	elif "AC" in line:
		dictA_atoms["C"] = line.split("=")[1].split(",");
	elif "AU" in line:
		dictA_atoms["U"] = line.split("=")[1].split(",");
	elif "AO" in line:
		dictA_atoms["O"] = line.split("=")[1].split(",");
	elif "baseAtoms" in line:
		baseAtoms = line.split("=")[1].split(",");
	elif "sugarAtoms" in line:
		sugarAtoms = line.split("=")[1].split(",");
	elif "backBoneAtoms" in line:
		backBoneAtoms = line.split("=")[1].split(",");
	elif "ions" in line:
                ions = line.split("=")[1].split(",");
	elif "water" in line:
		water = line.split("=")[1].split(",");
	elif "WCA" in line:
		WC["A"] = line.split("=")[1].split(",");
	elif "WCG" in line:
		WC["G"] = line.split("=")[1].split(",");
	elif "WCC" in line:
		WC["C"] = line.split("=")[1].split(",");
	elif "WCU" in line:
		WC["U"] = line.split("=")[1].split(",");
	elif "HA" in line:
		H["A"] = line.split("=")[1].split(",");
	elif "HG" in line:
		H["G"] = line.split("=")[1].split(",");
	elif "HC" in line:
		H["C"] = line.split("=")[1].split(",");
	elif "HU" in line:
		H["U"] = line.split("=")[1].split(",");
	elif "SA" in line:
		S["A"] = line.split("=")[1].split(",");
	elif "SG" in line:
		S["G"] = line.split("=")[1].split(",");
	elif "SC" in line:
		S["C"] = line.split("=")[1].split(",");
	elif "SU" in line:
		S["U"] = line.split("=")[1].split(",");
	ln = fin.readline();
fin.close();

dictDH = {};
#print dictD_atoms["O"];
# Note : since , C & U bases , both have C5 one of them is not included in the mapping files as both result in the same.

# It contains the mapping of which a respective donor interacts with a hydrogen atom. For e.g.: C2-H2 , C5'-H5' etc..

#fin = open("mappingDH.txt","r");
fin = open(loc+"cummap.txt","r");
ln = fin.readline();
while ln!= "":
	line = ln.split('\n')[0];
	vals = [];
	tmp = line.split("-");
	if tmp[0] in dictDH.keys():
		vals = dictDH[tmp[0]];
		vals.append(tmp[1]);
	else:
		vals.append(tmp[1]);
	dictDH[tmp[0]] = vals;
	ln = fin.readline();
fin.close();
ad={}
# To read adjacent atoms for all the heavy atoms.
fin = open(loc+"cumadj1.txt","r");
ln=fin.readline();
while ln!= "":
	line =ln.split('\n')[0];
	vals =[];
	tmp=line.split("-");
	ad[tmp[0]]=tmp[1]
	ln=fin.readline();
#	print ad;
fin.close();

list = [];	# Contains the list of each atom

count = 0;	# Respective counts of total atoms, donor, acceptor, hydrogen atoms.
countD = 0;
countA = 0;
countH = 0;

dictD = {};	# Respective dictionaries of donor, acceptor, hydrogen atoms.
dictA = {};
dictH = {};

# arr[a:b] => from a to b-1 (includes ath position & doesn't include the character at bth position)

numO5 = -1;
cDO3 = -1;
cO3 = -1;

prn = -1;
mrn = -1;

fp = open(sys.argv[1],"r");
ln = fp.readline();
while ln != "":
	each = [];
	coords = [];
	
	arr = ln.split('\n');
	ln = fp.readline();
	
	
	atom_num = arr[0][6:11].strip();	# These are according to the pdb file format which is in the current directory.
	atom_name = arr[0][12:16].strip();
	residue_name = arr[0][17:20].strip();
	residue_num = arr[0][21:26].strip();

	if count == 0:
		numO5 = residue_num;
		mrn = residue_num;
	if mrn != residue_num:
		flagO3 = -1;
		mrn = residue_num;
	if atom_name == "O3'":
		flagO3 = 1;
		cDO3 = countD;
		cO3 = count;
	if ln == "" and flagO3 == 1:
		dictD[cDO3] = cO3;
		countD += 1;
	if atom_name == "O5'" and numO5 == residue_num:
		dictD[countD] = count;
		countD += 1;
		numO5 = -1;

	if residue_name in dictD_atoms.keys() and (atom_name in dictD_atoms[residue_name] or atom_name in dictD_atoms["O"]):
		dictD[countD] = count;
		countD += 1;
	if residue_name in dictA_atoms.keys() and (atom_name in dictA_atoms[residue_name] or atom_name in dictA_atoms["O"]):
		dictA[countA] = count;
		countA += 1;
	elif atom_name == "OH2" and residue_name == "HOH":
		dictA[countA] = count;
		countA += 1;
		dictD[countD] = count;
		countD += 1;
	elif string.find(atom_name,"H") != -1:
		keyH = atom_name + "$" + residue_num ;
		dictH[keyH] = count;
#		print keyH
#		print dictH
#		print dictH[keyH]
#		print count
	elif atom_name == "MG" and residue_name == "MG":
		dictD[countD] = count;
		countD += 1;
	elif (atom_name == "POT" or atom_name == "K") and (residue_name == "POT" or residue_name == "K"):
                dictD[countD] = count;
                countD += 1;
	elif atom_name == "CL" and residue_name == "CL":
                dictD[countD] = count;
                countD += 1;
	elif atom_name == "CD" and residue_name == "CD":
                dictD[countD] = count;
                countD += 1;
	elif atom_name == "SR" and residue_name == "SR":
                dictD[countD] = count;
                countD += 1;
	elif atom_name == "BR" and residue_name == "BR":
                dictD[countD] = count;
                countD += 1;
	elif atom_name == "ZN" and residue_name == "ZN":
                dictD[countD] = count;
                countD += 1;
	elif (atom_name == "NA" or atom_name == "SOD") and (residue_name == "NA" or residue_name == "SOD"):
                dictD[countD] = count;
                countD += 1;
	elif atom_name == "OS" and residue_name == "OS":
                dictD[countD] = count;
                countD += 1;
	elif atom_name == "HG" and residue_name == "HG":
                dictD[countD] = count;
                countD += 1;
	count += 1;
	x = arr[0][30:38].strip();	y = arr[0][38:46].strip();	z = arr[0][46:54].strip();
	coords.append(x);	coords.append(y);	coords.append(z);
	each.append(atom_name);	each.append(coords);	each.append(atom_num);
	each.append(residue_name);	each.append(residue_num);	each.append(arr[0]);
	list.append(each);
#	residue_num=residue_num.strip();
#	print residue_num.strip();
	if residue_name == "A" or "G" or "C" or "U":
		adj[atom_name+residue_name+residue_num]=coords;
#	print adj;
#print dictD_atoms["O"];
# list[i][0] => ith atom name 
# list[i][1] => coordinates of that particular atom
# list[i][2] => ith atom number
# list[i][3] => residue name
# list[i][4] => residue number
# list[i][5] => the entire line read from the input file (in case for further developments)

# dictD contains the mapping of count of donor atom. For e.g.: dictD[0] = 4 => 1st donor atom is 4th atom in the list of atoms.
# similarly for acceptor atoms

#sys.exit();

outName = sys.argv[1] + ".out";	# Final output file => <inputfile>.out
print outName
fout = open(outName,"w");

outstr = "Bond\t\tDonor Information\t\t\tAcceptor Information\t\tHydrogen   Distance\tAngle\tClassification";
fout.write(outstr);
fout.write("\n\n");
outstr = "H-Bond\tNo.D\tat\trt\trn\tchNo\tNo.A\tat\trt\trn\tchNo\taH\tdDA\tdHA\taDHA\tty\tsub-ty\ttorsion";
fout.write(outstr);
fout.write("\n\n");

matrix = [];
for i in dictD.keys():
	indexD = dictD[i];
#	print indexD
	row = [];
	xD = float(list[indexD][1][0]);
	yD = float(list[indexD][1][1]);
	zD = float(list[indexD][1][2]);
	atnm = list[indexD][0];
	for j in dictA.keys():
		indexA = dictA[j];
		rsd = list[indexD][4];
	#	print "rsd id " + rsd;
		rsa = list[indexA][4];
		xA = float(list[indexA][1][0]);
		d=xD-xA;
		if d < 0 :
			d = d*-1;
		if d <= 5 :
			xA = float(list[indexA][1][0]);
			yA = float(list[indexA][1][1]);
			zA = float(list[indexA][1][2]);
	#		print list[indexA];
			distDA = sqrt( ((xD-xA)*(xD-xA)) + ((yD-yA)*(yD-yA)) + ((zD-zA)*(zD-zA)) );	# computing the bond-distance DA
			tmp = [];
			bondAngle = 0;
	#	if rsd != rsa and (xA-xD <=5.0 or xD-xA <= 5.0):
	#	if rsd != rsa:
			if rsd!=rsa and distDA >= float(dDA[0]) and distDA <= float(dDA[1]):
				if atnm == "MG" or atnm == "POT" or atnm == "K" or atnm == "CA" or atnm == "NA" or atnm == "SOD" or atnm == "CL" or atnm == "CD" or atnm == "SR" or atnm == "ZN" or atnm == "BR" or atnm == "OS" or atnm == "HG":
               		        	tmp.append(distDA);
					tmp.append("NA");
					tmp.append("NA");
					tmp.append("NA");
					tmp.append("NA");
               	                        tmp.append("NA");
                       	                tmp.append("NA");
				else:
					atomH = dictDH[list[indexD][0]];
#						print atomH;
#						print dictDH;
					for k in xrange(len(atomH)):
					#	print "out",k,atomH,len(atomH);						
						getKey = atomH[k] + "$" + rsd;
						if getKey in dictH.keys():
							noH = dictH[getKey];
							xH = float(list[noH][1][0]);
							yH = float(list[noH][1][1]);
							zH = float(list[noH][1][2]);
							distDH = sqrt( ((xD-xH)*(xD-xH)) + ((yD-yH)*(yD-yH)) + ((zD-zH)*(zD-zH)) );
							distHA = sqrt( ((xA-xH)*(xA-xH)) + ((yA-yH)*(yA-yH)) + ((zA-zH)*(zA-zH)) );
							bondAngle = acos( ((distDH*distDH)-(distDA*distDA)+(distHA*distHA))/(2*distDH*distHA) );
							bondAngle = bondAngle*(180*7.0/22.0);	# bond-angle DHA
						if bondAngle >= float(DHA[0]) and bondAngle <= float(DHA[1]):	# DHA[0,1] are from the input criteria file.
							if distHA >= float(dHA[0]) and distHA <= float(dHA[1]):
							
				#			han=noH
				#			print "in",k," ",atomH[k]
							#print noH
								ha=str(atomH[k])
			#			print ha
								tmp.append(distDA);
								tmp.append(distHA);
								tmp.append(bondAngle);
								tmp.append(ha);
								tmp.append(xH);
								tmp.append(yH);
								tmp.append(zH);

			row.append(tmp);
		else:
			a = [];
			row.append(a);
	flag = 0 ;
	for j in xrange(len(row)):
		if len(row[j]) != 0 and len(row[j]) >= 4:
			count += 1;
			first = "";
			# To find out the type of Hydrogen bond checks for the presence of carbon,nitrogen,oxygen in the donor and acceptor atoms
			if string.find(list[dictD[i]][0],"ZN") != -1:
                                first = "ZN";
			elif string.find(list[dictD[i]][0],"CA") != -1:
                                first = "CA";
			elif string.find(list[dictD[i]][0],"NA") != -1:
                                first = "NA";
			elif string.find(list[dictD[i]][0],"SOD") != -1:
                                first = "SOD";
			elif string.find(list[dictD[i]][0],"CL") != -1:
                                first = "CL";
			elif string.find(list[dictD[i]][0],"CD") != -1:
                                first = "CD";
			elif string.find(list[dictD[i]][0],"OS") != -1:
                                first = "OS";
			elif string.find(list[dictD[i]][0],"C") != -1:
				first = "C-H";
			elif string.find(list[dictD[i]][0],"N") != -1:
				first = "N-H";
			elif string.find(list[dictD[i]][0],"O") != -1:
				first = "O-H";
			else:
				first = str(atnm);
			if string.find(list[dictA[j]][0],"N") != -1:
				first += "--N";
			if string.find(list[dictA[j]][0],"O") != -1:
				first += "--O";
			frm=list[dictD[i]][4];
			frm1=frm.split(" ");
			frm5=len(frm1);
			if frm[1] != " ":
				frn=frm[-4:];
			else:
				frn=frm1[frm5-1];
			
			#print frm5;
			#print frm1;
			#print frn;
			frm2=list[dictA[j]][4]
			frm3=frm2.split(" ");
			frm6=len(frm3);
			if frm2[1] != " ":
                                frn1=frm2[-4:];
                        else:
                                frn1=frm3[frm6-1];
		#	print list[dictD[i]][0],list[dictD[i]][3],frn," "," ",list[dictA[j]][0],list[dictA[j]][3],frn1
			if str(row[j][1]) == "NA":
				outstr = first + "\t" + list[dictD[i]][2] + "\t" + list[dictD[i]][0] + "\t";
				outstr += list[dictD[i]][3] + "\t" +frn +"\t ";
				outstr += list[dictD[i]][5][21] + "\t";
				outstr += list[dictA[j]][2] + "\t" + list[dictA[j]][0] + "\t";
				outstr += list[dictA[j]][3] + "\t" +frn1 +"\t ";
				outstr += list[dictA[j]][5][21] + "\t" 
				outstr += str(row[j][3]) + "\t"
				outstr += str(round(row[j][0],3)) + "\t";
				outstr += str(row[j][1]) + "\t";
				outstr += str(row[j][2]) + "\t";
			else:
				outstr = first + "\t" + list[dictD[i]][2] + "\t" + list[dictD[i]][0] + "\t";
                                outstr += list[dictD[i]][3] + "\t" +frn +"\t ";
                                outstr += list[dictD[i]][5][21] + "\t";
                                outstr += list[dictA[j]][2] + "\t" + list[dictA[j]][0] + "\t";
                                outstr += list[dictA[j]][3] + "\t" +frn1 +"\t ";
                                outstr += list[dictA[j]][5][21] + "\t"
                                outstr += str(row[j][3]) + "\t"
                                outstr += str(round(row[j][0],3)) + "\t";
                                outstr += str(round(row[j][1],3)) + "\t";
                                outstr += str(round(row[j][2],3)) + "\t";
 
#			outstr += row[j][3] + "\t"
			flag = 1;
			# To find the type of atom that donor and acceptor belong to. whether a base/backbone/sugar atom.
			if list[dictD[i]][0] in baseAtoms:
				outstr += "b";
			elif list[dictD[i]][0] in backBoneAtoms:
				outstr += "B";
			elif list[dictD[i]][0] in sugarAtoms:
				outstr += "s";
			elif list[dictD[i]][0] in ions:
                                outstr += "I";
			elif list[dictD[i]][0] in water:
                                outstr += "W";
			if list[dictA[j]][0] in baseAtoms:
				outstr += "-b" + "\t";
			elif list[dictA[j]][0] in backBoneAtoms:
				outstr += "-B" + "\t";
			elif list[dictA[j]][0] in sugarAtoms:
				outstr += "-s" + "\t";
			elif list[dictA[j]][0] in water:
                                outstr += "-W" + "\t";
			# To find the sub-type of the hydrogen bond. whether it is watson crick or etc...
#			if (list[dictD[i]][0] in baseAtoms or (list[dictD[i]][0] in sugarAtoms and list[dictD[i]][0] == "O2'")) and list[dictA[j]][0] in baseAtoms:
			if (list[dictD[i]][0] in baseAtoms or (list[dictD[i]][0] in sugarAtoms and list[dictD[i]][0] == "O2'"))and (list[dictA[j]][0] in baseAtoms or (list[dictA[j]][0] in sugarAtoms and list[dictA[j]][0] == "O2'")):
#			if list[dictD[i]][0] in baseAtoms and list[dictA[j]][0] in baseAtoms:
			#	print ad[list[dictA[j]],list[dictA[j]][0],list[dictA[j]][3],
				xAd = adj[ad[list[dictA[j]][0]+list[dictA[j]][3]]+list[dictA[j]][4]][0];
				yAd = adj[ad[list[dictA[j]][0]+list[dictA[j]][3]]+list[dictA[j]][4]][1];
				zAd = adj[ad[list[dictA[j]][0]+list[dictA[j]][3]]+list[dictA[j]][4]][2];
#                               print ad[list[dictA[j]],list[dictA[j]][0],list[dictA[j]][3],
			#	print ad[list[dictD[j]][0]+list[dictD[j]][3]];
				xDd = adj[ad[list[dictD[i]][0]+list[dictD[i]][3]]+list[dictD[i]][4]][0];
				yDd = adj[ad[list[dictD[i]][0]+list[dictD[i]][3]]+list[dictD[i]][4]][1];
				zDd = adj[ad[list[dictD[i]][0]+list[dictD[i]][3]]+list[dictD[i]][4]][2];
				xH=row[j][4];
				yH=row[j][5];
				zH=row[j][6];
				xA=float(list[dictA[j]][1][0])
				yA=float(list[dictA[j]][1][1])
				zA=float(list[dictA[j]][1][2])
				#print xD,yD,zD,xH,yH,zH,xA,yA,zA,xAd,yAd,zAd
#			print torsion(xD,yD,zD,xH,yH,zH,xA,yA,zA,xAd,yAd,zAd);
				a1=xD-float(xDd) ;
                                a2=yD-float(yDd) ;
                                a3=zD-float(zDd) ;
                                b1=xA-xD ;
                                b2=yA-yD ;
                                b3=zA-zD ;
                                c1=float(xAd)-xA ;
                                c2=float(yAd)-yA ;
                                c3=float(zAd)-zA ;


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
#				outstr += str(round(ang,3))+" ";
				if row[j][3] in WC[list[dictD[i]][3]]:
					outstr += "W" + list[dictD[i]][3];
				elif row[j][3] in H[list[dictD[i]][3]]:
					outstr += "H" + list[dictD[i]][3];
				elif row[j][3] in S[list[dictD[i]][3]]:
					outstr += "S" + list[dictD[i]][3];
				if list[dictA[j]][0] in WC[list[dictA[j]][3]]:
					outstr += ":W" + list[dictA[j]][3];
				elif list[dictA[j]][0] in H[list[dictA[j]][3]]:
					outstr += ":H" + list[dictA[j]][3];
				elif list[dictA[j]][0] in S[list[dictA[j]][3]]:
					outstr += ":S" + list[dictA[j]][3];
				outstr +=" "+str(round(ang,3));

			if list[dictD[i]][0] in baseAtoms and list[dictA[j]][0] in backBoneAtoms:
				if (list[dictD[i]][0] == 'N1' and list[dictD[i]][3] == 'G' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P") ):
        	                     	outstr += "5BPhG"
				elif (list[dictD[i]][0] == 'N2' and row[j][3] == 'H22' and list[dictD[i]][3] == 'G' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
	                                outstr += "3BPhG"
				elif (list[dictD[i]][0] == 'N2' and row[j][3] == 'H21' and list[dictD[i]][3] == 'G' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                        	        outstr += "1BPhG"
				elif (list[dictD[i]][0] == 'C8' and list[dictD[i]][3] == 'G' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                	                outstr += "0BPhG"
				elif (list[dictD[i]][0] == 'N2' and list[dictD[i]][3] == 'A' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                	                outstr += "2BPhA"
				elif (list[dictD[i]][0] == 'C8' and list[dictD[i]][3] == 'A' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                	                outstr += "0BPhA"
				elif (list[dictD[i]][0] == 'N6' and row[j][3] == 'H62' and list[dictD[i]][3] == 'A' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
	                                outstr += "7BPhA"
				elif (list[dictD[i]][0] == 'N6' and row[j][3] == 'H61'and list[dictD[i]][3] == 'A' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                	                outstr += "6BPhA"
				elif (list[dictD[i]][0] == 'C5' and list[dictD[i]][3] == 'C' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
	                                outstr += "9BPhC"
        	                elif (list[dictD[i]][0] == 'C6' and list[dictD[i]][3] == 'C' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                	                outstr += "0BPhC"
                        	elif (list[dictD[i]][0] == 'N4' and row[j][3] == 'H42' and list[dictD[i]][3] == 'C' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")) :
                                	outstr += "7BPhC"
	                        elif (list[dictD[i]][0] == 'N4' and row[j][3] == 'H41' and list[dictD[i]][3] == 'C' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
        	                        outstr += "6BPhC"

				elif (list[dictD[i]][0] == 'C5' and list[dictD[i]][3] == 'U' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")): 
                        	        outstr += "9BPhU"
	                        elif (list[dictD[i]][0] == 'C6' and list[dictD[i]][3] == 'U' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")): 
        	                        outstr += "0BPhU"
				elif (list[dictD[i]][0] == 'N3' and list[dictD[i]][3] == 'U' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                        	        outstr += "5BPhU"
		
#			print "\n"
			fout.write(outstr);
			fout.write("\n");
                if len(row[j]) == 8:
			count += 1;
			first = "";
			# To find out the type of Hydrogen bond checks for the presence of carbon,nitrogen,oxygen in the donor and acceptor atoms
			if string.find(list[dictD[i]][0],"ZN") != -1:
                                first = "ZN";
                        elif string.find(list[dictD[i]][0],"CA") != -1:
                                first = "CA";
                        elif string.find(list[dictD[i]][0],"NA") != -1:
                                first = "NA";
                        elif string.find(list[dictD[i]][0],"SOD") != -1:
                                first = "SOD";
                        elif string.find(list[dictD[i]][0],"CL") != -1:
                                first = "CL";
                        elif string.find(list[dictD[i]][0],"CD") != -1:
                                first = "CD";
                        elif string.find(list[dictD[i]][0],"OS") != -1:
                                first = "OS";
                        elif string.find(list[dictD[i]][0],"C") != -1:
				first = "C-H";
			elif string.find(list[dictD[i]][0],"N") != -1:
				first = "N-H";
			elif string.find(list[dictD[i]][0],"O") != -1:
				first = "O-H";
			else:
				first = str(atnm);
			if string.find(list[dictA[j]][0],"N") != -1:
				first += "--N";
			if string.find(list[dictA[j]][0],"O") != -1:
				first += "--O";
			frm=list[dictD[i]][4];
			frm1=frm.split(" ");
			frm5=len(frm1);
			if frm[1] != " ":
				frn=frm[-4:];
			else:
				frn=frm1[frm5-1];
			
			#print frm5;
			#print frm1;
			#print frn;
			frm2=list[dictA[j]][4]
			frm3=frm2.split(" ");
			frm6=len(frm3);
			if frm2[1] != " ":
                                frn1=frm2[-4:];
                        else:
                                frn1=frm3[frm6-1];
		#	print list[dictD[i]][0],list[dictD[i]][3],frn," "," ",list[dictA[j]][0],list[dictA[j]][3],frn1
			if str(row[j][8]) == "NA":
				outstr = first + "\t" + list[dictD[i]][2] + "\t" + list[dictD[i]][0] + "\t";
				outstr += list[dictD[i]][3] + "\t" +frn +"\t ";
				outstr += list[dictD[i]][5][21] + "\t";
				outstr += list[dictA[j]][2] + "\t" + list[dictA[j]][0] + "\t";
				outstr += list[dictA[j]][3] + "\t" +frn1 +"\t ";
				outstr += list[dictA[j]][5][21] + "\t" 
				outstr += str(row[j][10]) + "\t"
				outstr += str(round(row[j][7],3)) + "\t";
				outstr += str(row[j][8]) + "\t";
				outstr += str(row[j][9]) + "\t";
			else:
				outstr = first + "\t" + list[dictD[i]][2] + "\t" + list[dictD[i]][0] + "\t";
                                outstr += list[dictD[i]][3] + "\t" +frn +"\t ";
                                outstr += list[dictD[i]][5][21] + "\t";
                                outstr += list[dictA[j]][2] + "\t" + list[dictA[j]][0] + "\t";
                                outstr += list[dictA[j]][3] + "\t" +frn1 +"\t ";
                                outstr += list[dictA[j]][5][21] + "\t"
                                outstr += str(row[j][10]) + "\t"
                                outstr += str(round(row[j][7],3)) + "\t";
                                outstr += str(round(row[j][8],3)) + "\t";
                                outstr += str(round(row[j][9],3)) + "\t";
 
#			outstr += row[j][3] + "\t"
			flag = 1;
			# To find the type of atom that donor and acceptor belong to. whether a base/backbone/sugar atom.
			if list[dictD[i]][0] in baseAtoms:
				outstr += "b";
			elif list[dictD[i]][0] in backBoneAtoms:
				outstr += "B";
			elif list[dictD[i]][0] in sugarAtoms:
				outstr += "s";
			elif list[dictD[i]][0] in ions:
                                outstr += "I";
			elif list[dictD[i]][0] in water:
                                outstr += "W";
			if list[dictA[j]][0] in baseAtoms:
				outstr += "-b" + "\t";
			elif list[dictA[j]][0] in backBoneAtoms:
				outstr += "-B" + "\t";
			elif list[dictA[j]][0] in sugarAtoms:
				outstr += "-s" + "\t";
			elif list[dictA[j]][0] in water:
                                outstr += "-W" + "\t";
			# To find the sub-type of the hydrogen bond. whether it is watson crick or etc...
#			if (list[dictD[i]][0] in baseAtoms or (list[dictD[i]][0] in sugarAtoms and list[dictD[i]][0] == "O2'")) and list[dictA[j]][0] in baseAtoms:
			if (list[dictD[i]][0] in baseAtoms or (list[dictD[i]][0] in sugarAtoms and list[dictD[i]][0] == "O2'"))and (list[dictA[j]][0] in baseAtoms or (list[dictA[j]][0] in sugarAtoms and list[dictA[j]][0] == "O2'")):
#			if list[dictD[i]][0] in baseAtoms and list[dictA[j]][0] in baseAtoms:
			#	print ad[list[dictA[j]],list[dictA[j]][0],list[dictA[j]][3],
				xAd = adj[ad[list[dictA[j]][0]+list[dictA[j]][3]]+list[dictA[j]][4]][0];
				yAd = adj[ad[list[dictA[j]][0]+list[dictA[j]][3]]+list[dictA[j]][4]][1];
				zAd = adj[ad[list[dictA[j]][0]+list[dictA[j]][3]]+list[dictA[j]][4]][2];
#                               print ad[list[dictA[j]],list[dictA[j]][0],list[dictA[j]][3],
			#	print ad[list[dictD[j]][0]+list[dictD[j]][3]];
				xDd = adj[ad[list[dictD[i]][0]+list[dictD[i]][3]]+list[dictD[i]][4]][0];
				yDd = adj[ad[list[dictD[i]][0]+list[dictD[i]][3]]+list[dictD[i]][4]][1];
				zDd = adj[ad[list[dictD[i]][0]+list[dictD[i]][3]]+list[dictD[i]][4]][2];
				xH=row[j][11];
				yH=row[j][12];
				zH=row[j][13];
				xA=float(list[dictA[j]][1][0])
				yA=float(list[dictA[j]][1][1])
				zA=float(list[dictA[j]][1][2])
				#print xD,yD,zD,xH,yH,zH,xA,yA,zA,xAd,yAd,zAd
#			print torsion(xD,yD,zD,xH,yH,zH,xA,yA,zA,xAd,yAd,zAd);
				a1=xD-float(xDd) ;
                                a2=yD-float(yDd) ;
                                a3=zD-float(zDd) ;
                                b1=xA-xD ;
                                b2=yA-yD ;
                                b3=zA-zD ;
                                c1=float(xAd)-xA ;
                                c2=float(yAd)-yA ;
                                c3=float(zAd)-zA ;


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
#				outstr += str(round(ang,3))+" ";
				if row[j][10] in WC[list[dictD[i]][3]]:
					outstr += "W" + list[dictD[i]][3];
				elif row[j][10] in H[list[dictD[i]][3]]:
					outstr += "H" + list[dictD[i]][3];
				elif row[j][10] in S[list[dictD[i]][3]]:
					outstr += "S" + list[dictD[i]][3];
				if list[dictA[j]][0] in WC[list[dictA[j]][3]]:
					outstr += ":W" + list[dictA[j]][3];
				elif list[dictA[j]][0] in H[list[dictA[j]][3]]:
					outstr += ":H" + list[dictA[j]][3];
				elif list[dictA[j]][0] in S[list[dictA[j]][3]]:
					outstr += ":S" + list[dictA[j]][3];
				outstr +=" "+str(round(ang,3));

			if list[dictD[i]][0] in baseAtoms and list[dictA[j]][0] in backBoneAtoms:
				if (list[dictD[i]][0] == 'N1' and list[dictD[i]][3] == 'G' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P") ):
        	                     	outstr += "5BPhG"
				elif (list[dictD[i]][0] == 'N2' and row[j][10] == 'H22' and list[dictD[i]][3] == 'G' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
	                                outstr += "3BPhG"
				elif (list[dictD[i]][0] == 'N2' and row[j][10] == 'H21' and list[dictD[i]][3] == 'G' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                        	        outstr += "1BPhG"
				elif (list[dictD[i]][0] == 'C8' and list[dictD[i]][3] == 'G' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                	                outstr += "0BPhG"
				elif (list[dictD[i]][0] == 'N2' and list[dictD[i]][3] == 'A' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                	                outstr += "2BPhA"
				elif (list[dictD[i]][0] == 'C8' and list[dictD[i]][3] == 'A' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                	                outstr += "0BPhA"
				elif (list[dictD[i]][0] == 'N6' and row[j][10] == 'H62' and list[dictD[i]][3] == 'A' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
	                                outstr += "7BPhA"
				elif (list[dictD[i]][0] == 'N6' and row[j][10] == 'H61'and list[dictD[i]][3] == 'A' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                	                outstr += "6BPhA"
				elif (list[dictD[i]][0] == 'C5' and list[dictD[i]][3] == 'C' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
	                                outstr += "9BPhC"
        	                elif (list[dictD[i]][0] == 'C6' and list[dictD[i]][3] == 'C' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                	                outstr += "0BPhC"
                        	elif (list[dictD[i]][0] == 'N4' and row[j][10] == 'H42' and list[dictD[i]][3] == 'C' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")) :
                                	outstr += "7BPhC"
	                        elif (list[dictD[i]][0] == 'N4' and row[j][10] == 'H41' and list[dictD[i]][3] == 'C' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
        	                        outstr += "6BPhC"

				elif (list[dictD[i]][0] == 'C5' and list[dictD[i]][3] == 'U' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")): 
                        	        outstr += "9BPhU"
	                        elif (list[dictD[i]][0] == 'C6' and list[dictD[i]][3] == 'U' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")): 
        	                        outstr += "0BPhU"
				elif (list[dictD[i]][0] == 'N3' and list[dictD[i]][3] == 'U' and (list[dictA[j]][0] == "O1P" or list[dictA[j]][0] == "O2P")):
                        	        outstr += "5BPhU"
		
#			print "\n"
			fout.write(outstr);
			fout.write("\n");
			
	if flag == 1:
		fout.write("\n");
		fout.write("\n");

fout.close();
