""" script to find different type of hydrogen bondings"""
import sys
from math import sqrt;
from math import acos;
loc = sys.argv[3] + '../src/tmp/'
counter = sys.argv[4];
def Cluster(filename):
    d = {}
    f = open( filename,'r')
    try:
        for line in f:
            l = []
            l.extend(line[0:5].split())
            l.extend(line[5:12].split())
            l.extend(line[13:17].split())
            l.extend(line[17:21].split())
            l.extend(line[21:22].split())
            l.extend(line[22:26].split())
            l.extend(line[30:38].split())
            l.extend(line[38:46].split())
            l.extend(line[46:54].split())
            key = l[5] + " " + l[4] + " " + l[3]
            if ( key in d ):
                d[key][l[2]] = l[6] + " " + l[7] + " " + l[8]
            else:
                d[key] = {}
                d[key][l[2]] = l[6] + " " + l[7] + " " + l[8]
    finally:
        return d
    f.close()

dict1 = Cluster(sys.argv[1])


def water(filename):
    """finds water mediated base pairs"""
    sys.stdout = open(loc+'watermediated_'+ counter +'.txt', 'w')
    file_name = open(filename, 'r')
    dic_wa = {}
    dic_wd = {}
    for line in file_name:
        split = line.split()
        if len(split) > 10:
            if split[15] == 'b-W' or split[15] == 'W-b':
                if split[3] == 'HOH':
                    dic_wd[split[3] + " " + split[4] + " " + split[5]] = split[8] + " " + split[9] + " " + split[10]
                if split[8] == 'HOH':
                    dic_wa[split[3] + " " + split[4] + " " + split[5]] = split[8] + " " + split[9] + " " + split[10]
            if (split[15] == 's-W' and split[2] == "O2'") or (split[15] == 'W-s' and split[7] == "O2'"):
                if split[3] == 'HOH':
                    dic_wd[split[3] + " " + split[4] + " " + split[5]] = split[8] + " " + split[9] + " " + split[10]
                if split[8] == 'HOH':
                    dic_wa[split[3] + " " + split[4] + " " + split[5]] = split[8] + " " + split[9] + " " + split[10]
    print "Water Mediated Base Pairs :"
    for i in dic_wa:
        for j in dic_wd:
            if dic_wa[i] == j and i != dic_wd[j]:
                print i + " " + dic_wa[i] + " " + j + " " + dic_wd[j]

def bifurcated(filename):
    """finds bifurcated pairs and triples"""
    file_name = open(filename, 'r')
    temp_dic = {}
    for line in file_name:
        split = line.split()
        if len(split) > 10 and split[3] != 'HOH' and split[8] != 'HOH':
            try:
                if (len(split[2]) == 2 or split[2][2] != "'" or split[2] == "O2'") and (len(split[7]) == 2 or split[7][2] != "'" or split[7] == "O2'"):
                    temp_dic[split[1] + " " + split[11] + " " + split[6]] = split[4]+split[5] + " " + split[9]+split[10] + " " + split[2]+split[3] + " " + split[11] + " " + split[7]+split[8]
            except:
                pass
    temp_dickeys = temp_dic.keys()
    pairs = []
    triples = []
    for i in xrange(len(temp_dickeys)):
        y = temp_dickeys[i].split(" ")
        for j in xrange(i+1, len(temp_dickeys)-1):
            z = temp_dickeys[j].split(" ")
            pb1 = temp_dic[temp_dickeys[i]].split(" ")
            qb1 = temp_dic[temp_dickeys[j]].split(" ")
            if y[0] == z[0] and y[1] != z[1] and y[2] == z[2]:
                outstr = "D--HH-A"+" "+pb1[0]+" "+ pb1[1]+" "+pb1[2]+" "+"<"+" "+pb1[3]+" "+qb1[3]+" "+ pb1[4]
                pairs.append(outstr)
                outstr = ""
            elif y[0] == z[0] and y[1] == z[1] and y[2] != z[2]:
                if pb1[1] == qb1[1]:
                    outstr = "D--H-AA" + " " + pb1[0] + " " + pb1[1] + " " + pb1[2] + " " + "-" + " " + pb1[3] + " " + "<" + " " + pb1[4] + " " + qb1[4]
                    pairs.append(outstr)
                    outstr = ""
                else:
                    outstr = "D--H-AA" + " " + pb1[0] + " " + pb1[1] + " " + "&" + " " + qb1[1] + " " + pb1[2] + " " + "-" + " " + pb1[3] + " " + "<" + " " + pb1[4] + " " + qb1[4]
                    triples.append(outstr)
                    outstr = ""
            elif y[0] != z[0] and y[1] != z[1] and y[2] == z[2]:
                if pb1[0] == qb1[0]:
                    outstr = "DD-HH-A" + " " + pb1[0] + " " + pb1[1] + " " + pb1[2] + " " + qb1[2] + " " + "-" + " " + pb1[3] + " " + qb1[3] + " " + ">" + " " + pb1[4]
                    pairs.append(outstr)
                    outstr = ""
                else:
                    outstr = "DD-HH-A" + " " + pb1[0] + " " + "&" + " " + qb1[0] + " " + pb1[1] + " " + pb1[2] + " " + qb1[2] + " " + "-" + " " + pb1[3] + " " + qb1[3] + " " + ">" + " " + pb1[4]
                    triples.append(outstr)
                    outstr = ""
            elif y[0] == z[0] and y[1] != z[1] and y[2] != z[2]:
                if pb1[1] == qb1[1]:
                    outstr = "D-HH-AA" + " " + pb1[0] + " " + pb1[1] + " " + pb1[2] + " " + "<" + " " + pb1[3] + " " + qb1[3] + " " + ">" + " " + pb1[4] + " " + qb1[4]
                    pairs.append(outstr)
                    outstr = ""
                else:
                    outstr = "D-HH-AA" + " " + pb1[0] + " " + pb1[1] + " " + "&" + " " + qb1[1] + " " + pb1[2] + " " + "<" + " " + pb1[3] + " " + qb1[3] + " " + ">" + " " + pb1[4] + " " + qb1[4]
                    triples.append(outstr)
                    outstr = ""
            elif z[0] != y[0] and z[1] != y[1] and y[2] != z[2]: 
                if z[2] == y[0]:
            	    if pb1[0] == qb1[1]:
            	        outstr = "D-H-A-D" + " " + pb1[0] + " " + pb1[1] + " " + " " + pb1[2] + " " + pb1[3] + " " + pb1[4] + " " + qb1[3] + " " + qb1[4]
                        pairs.append(outstr)
                        outstr = ""
            	    else:
                        outstr = "D-H-A-D" + " " + pb1[0] + " " + pb1[1] + " " + qb1[1] + " " + pb1[2] + " " + pb1[3] + " " + pb1[4] + " " + qb1[3] + " " + qb1[4]
                        triples.append(outstr)
                        outstr = ""
                elif z[0] == y[2]:
                    if pb1[0] == qb1[1]:
                        outstr = "D-H-A-D" + " " + pb1[0] + " " + pb1[1] + " " + " " + pb1[2] + " " + pb1[3] + " " + pb1[4] + " " + qb1[3] + " " + qb1[4]
                        pairs.append(outstr)
                        outstr = ""
                    else:
                        outstr = "D-H-A-D" + " " + pb1[0] + " " + pb1[1] + " " + qb1[1] + " " + pb1[2] + " " + pb1[3] + " " + pb1[4] + " " + qb1[3] + " " + qb1[4]
                        triples.append(outstr)
                        outstr = ""
    
    r = []
    for i in xrange(1, 6):
        k = []
        r.append(k)
    sys.stdout = open(loc+'bifurcated_pairs_'+ counter +'.txt', 'w')
    print "Bifuricated Pairs : "
    for i in xrange(len(pairs)):
        c = pairs[i].split()
        if c[0] == "D-HH-A":
            r[5].append(pairs[i])
        if c[0] == "D-H-A-D":
            r[4].append(pairs[i])
        if c[0] == "DD-HH-A":
            r[3].append(pairs[i])
        if c[0] == "D-HH-AA":
            r[2].append(pairs[i])
        if c[0] == "D--H-AA":
            r[1].append(pairs[i])
    for i in xrange(1, 5):
        for j in xrange(len(r[i])):
            print r[i][j]
    r = []
    for i in xrange(1, 6):
        k = []
        r.append(k)
    print "\n"
    sys.stdout = open(loc+'bifurcated_triplets_'+ counter +'.txt', 'w')
    print "Bifuricated Triples : "
    for i in xrange(len(triples)):
        c = triples[i].split()
        if c[0] == "D-HH-A":
            r[5].append(triples[i])
        if c[0] == "D-H-A-D":
            r[4].append(triples[i])
        if c[0] == "DD-HH-A":
            r[3].append(triples[i])
        if c[0] == "D-HH-AA":
            r[2].append(triples[i])
        if c[0] == "D--H-AA":
            r[1].append(triples[i])
    for i in xrange(1, 5):
        for j in xrange(len(r[i])):
            print r[i][j]

def basepairs(filename):
    sys.stdout = open(loc+'basepairs_'+ counter +'.txt', 'w')
    file_name = open(filename, 'r')
    dic = {}
    print "Base Pairs : "
    for lines in file_name:
        d = lines.split()
        if len(d)>15:
            if d[15] == 'b-b' or (d[15] == 's-b' and d[2] == "O2'") or (d[15] == 'b-s' and d[7] == "O2'"):
                key = (d[4] + " " + d[5] + " " + d[3], d[9] + " " + d[10] + " " + d[8])
                oppkey=(d[9] + " " + d[10] + " " + d[8], d[4] + " " + d[5] + " " + d[3])
                if(dic.has_key(key)):
                    dic[key].append(lines)
                elif(dic.has_key(oppkey)):
                    dic[oppkey].append(lines)
                else:
                    dic[key]=[]
                    dic[key].append(lines)
    for key in sorted(dic.iterkeys(), lambda x, y: 1 if int(x[0].split(" ")[0]) > int(y[0].split(" ")[0]) else -1): 
        d = dic[key][0].split()
        b = key[0]
        c = key[1]
        if( (b[-1] == 'A' or b[-1] == 'G' )and b[-2] == " "):
            temp=dict1[b]
            x = 0.0
            y = 0.0
            z = 0.0
            for j in temp:
                if j == 'C4' or j == 'C5' or j == 'N7' or j == 'C8'or j == 'N9':
                    t = temp[j].split()
                    x = x + float(t[0])
                    y = y + float(t[1])
                    z = z + float(t[2])
                x2 = round(x/5, 3)
                y2 = round(y/5, 3)
                z2 = round(z/5, 3)
                c1 = dict1[b]["C1'"].rsplit()

        elif( (b[-1] == 'C' or b[-1] == 'U' )and b[-2] == " "):
            temp=dict1[b]
            x = 0.0
            y = 0.0
            z = 0.0
            for j in temp:
                if j == 'N1' or j == 'C2' or j == 'N3' or j == 'C4'or j == 'C5' or j == 'C6':
                    t = temp[j].split()
                    x = x + float(t[0])
                    y = y + float(t[1])
                    z = z + float(t[2])
                x2 = round(x/6, 3)
                y2 = round(y/6, 3)
                z2 = round(z/6, 3)
                c1 = dict1[b]["C1'"].rsplit()
        try:
            x1 = float(c1[0])
            y1 = float(c1[1])
            z1 = float(c1[2])
        except:
            pass
        if(c[-1] == 'C' or c[-1] == 'U') and c[-2] == " ":
            temp1= dict1[c]
            x = 0.0
            y = 0.0
            z = 0.0
            for j in temp1:
                if j == 'N1' or j == 'C2' or j == 'N3' or j == 'C4'or j == 'C5' or j == 'C6':
                    t = temp1[j].split()
                    x = x + float(t[0])
                    y = y + float(t[1])
                    z = z + float(t[2])
            x3 = round(x/6, 3)
            y3 = round(y/6, 3)
            z3 = round(z/6, 3)
            c2 = dict1[c]["C1'"].rsplit();

        elif(c[-1] == 'G' or c[-1] == 'A')and c[-2] == " ":
            temp1 = dict1[c]
            x = 0.0
            y = 0.0
            z = 0.0
            for j in temp1:
                if j == 'C4' or j == 'C5' or j == 'N7' or j == 'C8'or j == 'N9':
                    t = temp1[j].split()
                    x = x + float(t[0])
                    y = y + float(t[1])
                    z = z + float(t[2])
            x3 = round(x/5, 3)
            y3 = round(y/5, 3)
            z3 = round(z/5, 3)
            c2 = dict1[c]["C1'"].rsplit()
        try:
            x4 = float(c2[0])
            y4 = float(c2[1])
            z4 = float(c2[2])
        except:
            pass
        a1 = x2 - x1
        a2 = y2 - y1
        a3 = z2 - z1
        b1 = x3 - x2
        b2 = y3 - y2
        b3 = z3 - z2
        c1 = x4 - x3
        c2 = y4 - y3
        c3 = z4 - z3
        a4 = sqrt((a1 * a1) + (a2 * a2) + (a3 * a3))
        b4 = sqrt((b1 * b1) + (b2 * b2) + (b3 * b3))
        c4 = sqrt((c1 * c1) + (c2 * c2) + (c3 * c3))

        A = ((a2 * b3) - (a3 * b2))
        A = A / (a4 * b4)
        B = ((a1 * b3) - (a3 * b1)) / (a4 * b4)
        C = ((a1 * b2) - (a2 * b1)) / (a4 * b4)
        X = ((b2 * c3) - (b3 * c2)) / (b4 * c4)
        Y = ((b1 * c3) - (b3 * c1)) / (b4 * c4)
        Z = ((b1 * c2) - (b2 * c1)) / (b4 * c4)
        p = (A * X) + (B * Y) + (C * Z)
        q = (sqrt((A * A) + (B * B) + (C * C))) * (sqrt((X * X) + (Y * Y) + (Z * Z)))
        ang = acos(p / q)
        ang = (ang * 7 * 180) / 22
			# Converting the angle into degrees
        Type = ''
        if ang > 90:
            Type = 'Trans'
        else:
            Type = 'Cis'
            ang = str(ang)

        print d[4] + "" + d[5] + " " + d[9] + "" + d[10] + " " + d[3] + ":" + d[8] + " " + d[16][0] + ":" + d[16][3],Type, len(dic[key])


if __name__ == '__main__':
    water(sys.argv[2])
    bifurcated(sys.argv[2])
    basepairs(sys.argv[2])
