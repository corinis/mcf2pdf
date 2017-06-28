package net.sf.mcf2pdf.util;

import java.io.File;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import net.sf.mcf2pdf.Main;
import net.sf.mcf2pdf.mcfelements.util.ImageUtil;

public class ImageUtilTest {

	@Test
	@Ignore("Currently disabled as no longer used by CEWE?!")
	public void testPngWithRes() throws Exception {
		float[] res = ImageUtil.getImageResolution(new File("./src/test/resources/Bertrand1-figure-withres.png"));
		Assert.assertEquals(72.009f, res[0], 0.01f);
		Assert.assertEquals(72.009f, res[1], 0.01f);
	}

	@Test
	public void testPngWithoutRes() throws Exception {
		float[] res = ImageUtil.getImageResolution(new File("./src/test/resources/Bertrand1-figure.png"));
		Assert.assertEquals(180.0f, res[0], 0.01f);
		Assert.assertEquals(180.0f, res[1], 0.01f);
	}

	@Test
	public void testRender() throws Exception {
		
		Main.main(new String[] {"-b", "-i", "/home/gargan/software/BIPA", "-r", "300", "-n", "7", "-t", "/home/gargan/.mcf",
				"/home/gargan/FotobuchArgentinien/Argentinien 2013.mcf", "/home/gargan/1test.pdf"});
	}

	@Test
	public void testFo() throws Exception {
		
		Main.main(new String[] {"-b", "-i", "/home/gargan/software/BIPA", "-x", "-r", "300", "-n", "3", "-t", "/home/gargan/.mcf",
				"/home/gargan/FotobuchArgentinien/Argentinien 2013.mcf", "/home/gargan/1test.fo"});
	}
}
