#!/usr/bin/python
"""
To present the output as per requirements
output files:
	nomenclature
"""

import sys

nuc_i=[]
nuc_j=[]
type_i=[]
type_j=[]
ring_i=[]	# 5 or 6-membered ring
ring_j=[]
chain_i=[]
chain_j=[]
distance=[]
shift=[]
slide=[]
rise=[]
roll=[]
tilt=[]
twist=[]

loc = sys.argv[1] + "../src/tmp/"
cnt = sys.argv[2]

# Take output of code2 'stacked_bases' as input
fin=open(loc + 'stacked_bases_' + cnt, 'r')
for line in fin:
	columns = line.split('\t')
	columns = [col.strip() for col in columns]
	nuc_i.append(columns[0])
	type_i.append(columns[1])
	ring_i.append(columns[2])
	chain_i.append(columns[3])
	nuc_j.append(columns[4])
	type_j.append(columns[5])
	ring_j.append(columns[6])
	chain_j.append(columns[7])
	distance.append(columns[8])
	shift.append(columns[9])
	slide.append(columns[10])
	rise.append(columns[11])
	roll.append(columns[12])
	tilt.append(columns[13])
	twist.append(columns[14])
fin.close()

# Write the stacked pairs as per requirement in file 'nomen'
fout=open(loc + 'nomen_' + cnt,"w")

#fout.write("\nnomenclature\t\tdistance\tshift\tslide\trise\troll\ttilt\ttwist\n")
rest_nuc_i=[]
rest_nuc_j=[]
rest_type_i=[]
rest_type_j=[]
rest_ring_i=[]
rest_ring_j=[]
rest_chain_i=[]
rest_chain_j=[]
rest_shift=[]
rest_slide=[]
rest_rise=[]
rest_roll=[]
rest_tilt=[]
rest_twist=[]

fout.write("\nSTACKED PAIRS\n")

fout.write("\ndistant stacking\n\n")

i=1
while (i<len(nuc_i)):
	if ((nuc_j[i][-1] in ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z']) or (nuc_i[i][-1] in ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'])):	
		rest_nuc_i.append(nuc_i[i])
		rest_nuc_j.append(nuc_j[i])
		rest_type_i.append(type_i[i])
		rest_type_j.append(type_j[i])
		rest_ring_i.append(ring_i[i])
		rest_ring_j.append(ring_j[i])
		rest_chain_i.append(chain_i[i])
		rest_chain_j.append(chain_j[i])
		rest_shift.append(shift[i])
		rest_slide.append(slide[i])
		rest_rise.append(rise[i])
		rest_roll.append(roll[i])
		rest_tilt.append(tilt[i])
		rest_twist.append(twist[i])
		del nuc_i[i]
		del nuc_j[i]
		del type_i[i]
		del type_j[i]
		del ring_i[i]
		del ring_j[i]
		del chain_i[i]
		del chain_j[i]
		del shift[i]
		del slide[i]
		del rise[i]
		del roll[i]
		del tilt[i]
		del twist[i]
		continue
	elif (int(nuc_j[i]) != int(nuc_i[i])+1):
	#	fout.write(nuc_i[i]+type_i[i]+"("+ring_i[i]+")-"+chain_i[i]+"||"+nuc_j[i]+type_j[i]+"("+ring_j[i]+")-"+chain_j[i]+"\t\t"+distance[i]+"\t"+shift[i]+"\t"+slide[i]+"\t"+rise[i]+"\t"+roll[i]+"\t"+tilt[i]+"\t"+twist[i]+"\n")
		fout.write(nuc_i[i]+type_i[i]+"("+ring_i[i]+")-"+chain_i[i]+"||"+nuc_j[i]+type_j[i]+"("+ring_j[i]+")-"+chain_j[i]+"\n")
		del nuc_i[i]
		del nuc_j[i]
		del type_i[i]
		del type_j[i]
		del ring_i[i]
		del ring_j[i]
		del chain_i[i]
		del chain_j[i]
		del shift[i]
		del slide[i]
		del rise[i]
		del roll[i]
		del tilt[i]
		del twist[i]
		continue
	i=i+1

fout.write("\nconsecutive stacking\n\n")

i=1
while (i<len(nuc_i)):
	if (i+1==len(nuc_i)):
	#	fout.write(nuc_i[i]+type_i[i]+"("+ring_i[i]+")-"+chain_i[i]+"||"+nuc_j[i]+type_j[i]+"("+ring_j[i]+")-"+chain_j[i]+"\t\t"+distance[i]+"\t"+shift[i]+"\t"+slide[i]+"\t"+rise[i]+"\t"+roll[i]+"\t"+tilt[i]+"\t"+twist[i]+"\n")
		fout.write(nuc_i[i]+type_i[i]+"("+ring_i[i]+")-"+chain_i[i]+"||"+nuc_j[i]+type_j[i]+"("+ring_j[i]+")-"+chain_j[i]+"\n")
		break
	if ((int(nuc_j[i])!=int(nuc_i[i+1])) and (int(nuc_j[i])!=int(nuc_i[i+1])+1)):
	#	fout.write(nuc_i[i]+type_i[i]+"("+ring_i[i]+")-"+chain_i[i]+"||"+nuc_j[i]+type_j[i]+"("+ring_j[i]+")-"+chain_j[i]+"\t\t"+distance[i]+"\t"+shift[i]+"\t"+slide[i]+"\t"+rise[i]+"\t"+roll[i]+"\t"+tilt[i]+"\t"+twist[i]+"\n")
		fout.write(nuc_i[i]+type_i[i]+"("+ring_i[i]+")-"+chain_i[i]+"||"+nuc_j[i]+type_j[i]+"("+ring_j[i]+")-"+chain_j[i]+"\n")
	else:
	#	fout.write(nuc_i[i]+type_i[i]+"("+ring_i[i]+")-"+chain_i[i]+"||"+nuc_j[i]+type_j[i]+"("+ring_j[i]+")-"+chain_j[i]+"\t\t"+distance[i]+"\t"+shift[i]+"\t"+slide[i]+"\t"+rise[i]+"\t"+roll[i]+"\t"+tilt[i]+"\t"+twist[i]+"\n")
		fout.write(nuc_i[i]+type_i[i]+"("+ring_i[i]+")-"+chain_i[i]+"||"+nuc_j[i]+type_j[i]+"("+ring_j[i]+")-"+chain_j[i]+"\n")
	i=i+1

if (len(rest_nuc_i) != 0):
	fout.write("\nrest stacking\n\n")
	i=0
	while (i<len(rest_nuc_i)):
		fout.write(rest_nuc_i[i]+rest_type_i[i]+"("+rest_ring_i[i]+")-"+rest_chain_i[i]+"||"+rest_nuc_j[i]+rest_type_j[i]+"("+rest_ring_j[i]+")-"+rest_chain_j[i]+"\n")
		i=i+1

fout.close()
# VJ
