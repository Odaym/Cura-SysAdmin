package com.cura.classes;

public class Bash {
	public static final String getMemoryOutput = "free | awk '{if (NR > 1) m = 4;else m = 3;l = $0;for (i = 1; i <= m; i++) {o[i] = index(l,$i) + length($i) - 1; l = substr(l,o[i] - 1)} for (i = 1; i <= m; i++) printf(\"%*s--\",o[i],$i);print \"\"}'";
	public static final String getProcessStatus = "ps axo pid,user,pmem,pcpu,comm | { IFS= read -r header; echo \"$header\"; sort -k 3,3nr; } | head -7";
	public static final String getProcessIDs = "ps axo pid,user,pmem,pcpu,comm | { IFS= read -r header; sort -k 3,3nr; } | head -7 | awk '{print $5}'";
	public static final String getLastBootTime = "last reboot | head -1 | awk '{print $5 \" \" $6 \" \" $7 \" \" $8 \" \" $9 \" \" $10 \" \" $11}'";
	public static final String getCurrentUsers = "who | awk '{print $1}' | uniq | wc -l | xargs /bin/echo -n";
	public static final String getCurrentUsersNames = "who | awk '{print $1}' | uniq | xargs /bin/echo -n";
	public static final String getUptime = "uptime | awk '{print $2 \"\t \" $3 \" \" $4 \" \" $5}'";
	public static final String getLoadAverages = "uptime | awk '{print $10 \" \" $11 \" \" $12}'";
	public static final String getKernelVersion = "uname -mrsv";
	public static final String getKillProcess = "kill `pidof ";
	public static final String getHostname = "hostname";
	public static final String getFilesystems = "df -h";
  public static final String getCPU = "ps aux | awk '{sum+=$3} END {print sum}'";
  public static final String getRAM = "ps aux | awk '{sum+=$4} END {print sum}'";
}
