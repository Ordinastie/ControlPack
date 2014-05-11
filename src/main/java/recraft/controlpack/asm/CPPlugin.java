package recraft.controlpack.asm;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import recraft.controlpack.ControlPack;

import com.google.common.base.Throwables;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@TransformerExclusions({ "recraft." })
// @IFMLLoadingPlugin.SortingIndex(1001)
public class CPPlugin implements IFMLLoadingPlugin
{

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[] { CPTransformer.class.getName() };
	}

	@Override
	public String getModContainerClass()
	{
		return ControlPack.class.getName();
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		ControlPack.coremodLocation = (File) data.get("coremodLocation");
		if (ControlPack.coremodLocation == null)
		{ 
			try
			{
				ControlPack.coremodLocation = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			}
			catch (URISyntaxException e)
			{
				System.err.println("Failed to acquire source location for ControlPack!");
				throw Throwables.propagate(e);
			}
		}
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}

}
