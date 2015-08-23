#coor file and out file as arguments 
#!usr/bin/perl
$a=$ARGV[0];
$b=$ARGV[1];
print "Include single H-bonded base pair(y/n):";
$bonds=<STDIN>;
if ($bonds =~ "y")
{
$bonds=1;
}
else 
{
$bonds=2;
}
open(fp,"$a");
open(fp1,"$b");
$i=0;
@aoa=();
while(<fp1>)
{
	@arr=();
	@arr=split(/\s+/,$_);
	$aoa[$i++] = [ @arr ];
}
<fp>;
$min=0;
$max=0;
$l=0;
$a=0;
@chainv=();@minv=();@maxv=();

while(<fp>)
{
	@pos=();
	$chain=substr($_,21,1);
	$no=substr($_,22,4);
	$str="$no"."$chain";
	@pos=split(/\s+/,$_);
	if($l !=0 && $a eq $str)
	{
		next;
	}
	else
	{
		$l=0;
	}
	if($l==0)
	{
		$a=$str;
		@fin=split(/\s+/,$a);
		$l=1;
		$q=0;
		for $i ( 0 .. $#aoa ) 
		{

			if($fin[1] eq $aoa[$i][0] && $aoa[$i][5]>=$bonds)
			{
				if( $aoa[$i][4] eq "Cis")
				{
					if ($aoa[$i][6] eq "HOH")
					{
						print "$aoa[$i][0] $aoa[$i][2] $aoa[$i][3]C $aoa[$i][1] $aoa[$i][5] W $aoa[$i][7]\t"
					}
					else
					{
						print "$aoa[$i][0] $aoa[$i][2] $aoa[$i][3]C $aoa[$i][1] $aoa[$i][5]\t\t";
					}
				}
				if( $aoa[$i][4] eq "Trans")
				{
					if ($aoa[$i][6] eq "HOH")
                                        {
                                                print "$aoa[$i][0] $aoa[$i][2] $aoa[$i][3]T $aoa[$i][1] $aoa[$i][5] W $aoa[$i][7]\t"
                                        }
                                        else
                                        {
                                                print "$aoa[$i][0] $aoa[$i][2] $aoa[$i][3]T $aoa[$i][1] $aoa[$i][5]\t\t";
                                        }

				}
				$q=1;
			}
			if($fin[1] eq $aoa[$i][1] && $aoa[$i][5]>=$bonds)
                        {
				@p1=split(/:/,$aoa[$i][2]);
				@p2=split(/:/,$aoa[$i][3]);
                                if( $aoa[$i][4] eq "Cis")
                                {
					if ($aoa[$i][6] eq "HOH")
					{
	                                	print "$aoa[$i][1] $p1[1]:$p1[0] $p2[1]:$p2[0]C $aoa[$i][0] $aoa[$i][5] W $aoa[$i][7]\t";
					}
					else
					{
						print "$aoa[$i][1] $p1[1]:$p1[0] $p2[1]:$p2[0]C $aoa[$i][0] $aoa[$i][5]\t\t"
					}
                                }
                                if( $aoa[$i][4] eq "Trans")
                                {
					if ($aoa[$i][6] eq "HOH")
					{
                                        	print "$aoa[$i][1] $p1[1]:$p1[0] $p2[1]:$p2[0]T $aoa[$i][0] $aoa[$i][5] W $aoa[$i][7]\t";
					}
					else
					{
						print "$aoa[$i][1] $p1[1]:$p1[0] $p2[1]:$p2[0]T $aoa[$i][0] $aoa[$i][5]\t\t";
					}                                
}

                                $q=1;
                        }


		}
		if($q==0)
		{
			print "$fin[1] $pos[3]";
		}
		print "\n";
	}
}
