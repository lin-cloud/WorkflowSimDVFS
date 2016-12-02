package org.workflowsim.utils;

import java.io.FileWriter;

import org.cloudbus.cloudsim.Log;

public final class ExportCsv
{
	private static boolean export;
	private static FileWriter writer;
	private static double tempUtilization;
	private static int tempMultiplier;
	private static double tempPower;
	private static double tempTime;
	private static int index;
	
	private ExportCsv(){}
	
	public static boolean doExport()
	{
		return export;
	}
	
	public static void setExport(boolean export, String file)
	{
		ExportCsv.export = export;
		if (ExportCsv.export)
		{
			initialize(file);
		}
	}
	
	public static void initialize(String file)
	{
		
		try
		{
			ExportCsv.writer = new FileWriter(file);
			
			String initialize = "";
			initialize += "Utilization";
			initialize += ";";
			initialize += "Multiplier";
			initialize += ";";
			initialize += "Power";
			initialize += ";";
			initialize += "Time";
			initialize += "\n";
			
			ExportCsv.writer.append(initialize);
		}
		catch(Exception e)
		{
			Log.printLine("Exception: " + e);
		}
	}
	
	public static void setTempUtilization(double utilization)
	{
		ExportCsv.tempUtilization = utilization;
	}
	
	public static int getTempMultiplier()
	{
		return tempMultiplier;
	}
	
	public static void setTempMultiplier(int multiplier)
	{
		ExportCsv.tempMultiplier = multiplier;
	}
	
	public static void setTempPower(double power)
	{
		ExportCsv.tempPower = power;
	}
	
	public static void setTempTime(double time)
	{
		ExportCsv.tempTime = time;
	}
	
	public static void addToCsv()
	{
		String row = "";
		row += ExportCsv.tempUtilization;
		row += ";";
		row += ExportCsv.tempMultiplier;
		row += ";";
		row += ExportCsv.tempPower;
		row += ";";
		row += ExportCsv.tempTime;
		row += "\n";
		try
		{
			ExportCsv.writer.append(row);
		}
		catch(Exception e)
		{
			Log.printLine("Exception: " + e);
		}
	}
	
	public static void finishCsv()
	{
		try
		{
			ExportCsv.writer.flush();
			ExportCsv.writer.close();
		}
		catch(Exception e)
		{
			Log.printLine("Exception: " + e);
		}
	}
	
	public static int getIndex()
	{
		return index;
	}
	
	public static void setIndex(int index)
	{
		ExportCsv.index = index;
	}
}