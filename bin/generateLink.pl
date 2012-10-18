#!/usr/bin/perl


%obj = map { $_ => 1 } qw(Activation DatabaseImage Directory GsonTransient ImageAttribute ImageSet ImageSetMembership Paper PermissionedModel Permission Project TemplateAssignment TemplateAttribute Template TimestampModel User);

$ARGV[0] =~ m/\/([^\/]*?).java$/;
$className = $1;
while(<>) {
	if (/public static void (.*)\((.*)\)/) {
		$method = $1;
		print "this.$method = ";
		@vals = split(",",$2);
		#if ($1 =~ /^get/) {
		#	if (scalar(@vals) > 0) {
		#		print "new VariableLink(";
		#	} else {
		#		print "new Link(";
		#	}
		#} else {
		#	print "new Action(";
		#}
		print "new Loader(";
		print "\"\@{$className.$method}";
		$accum = "";
		$index = -1;
		foreach $param (@vals) {
			$index ++;
			if ($index > 0) {
				print "&";
			} else {
				print "?";
			}
			$param =~ s/^\s+//;
			@var = split(" ",$param);
			if (scalar(@vals) > 0) {
				if ($obj{$var[0]}) {
					$name = "{$var[1]Id}";
				} else {
					$name = "{$var[1]}";
				}
			} else {
				$name = "{$index}";
			}
			if ($obj{$var[0]}) {
				print "$var[1].id=$name";
			} else {
				print "$var[1]=$name";
			}
		}
		print "\");";
		print "\n";
	}
}
