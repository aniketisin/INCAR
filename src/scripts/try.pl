#to turn 3 letter into 1 letter
open(IN,"$ARGV[$1]")||die "error opening file";
$^I ='.bkp';  
while(<>)
{
	chomp;
#	print $_;
	if(/CYT/)
	{
	#	print "$_\n";
		s/CYT/  C/g;
#		print OUT $_;
		print ;
		print "\n";
	}
	elsif(/GUA/)
	{
		s/GUA/  G/g;
		print ;
		print "\n";
	}
	elsif(/URA/)
	{
		s/URA/  U/g;
       		print ;
		print "\n";	
	 }
	elsif(/THY/)
        {
                s/THY/  T/g;
                print ;
      		print "\n"; 
	 }
	elsif(/ADE/)
        {
                s/ADE/  A/g;
                print ;
       		print "\n";
	 }
	elsif(/OH2 TIP3/)
        {
                s/OH2 TIP3/OH2 HOH /g;
                print ;
      		print "\n";
	 }
	elsif(/H2  TIP3/)
        {
                s/H2  TIP3/H2O HOH /g;
                print ;
      		print "\n";
	 }
	elsif(/H1  TIP3/)
        {
                s/H1  TIP3/H1O HOH /g;
                print ;
 	       print "\n";
	}
       elsif(/ /)
        {
		s/ / /g;
		print ;
		print "\n";
	}
	
}
