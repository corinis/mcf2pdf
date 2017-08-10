/*******************************************************************************
 * ${licenseText}
 * All rights reserved. This file is made available under the terms of the
 * Common Development and Distribution License (CDDL) v1.0 which accompanies
 * this distribution, and is available at
 * http://www.opensource.org/licenses/cddl1.txt
 *******************************************************************************/
package net.sf.mcf2pdf.pagebuild;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import net.sf.mcf2pdf.mcfelements.McfArea;
import net.sf.mcf2pdf.mcfelements.util.XslFoDocumentBuilder;

import org.apache.fop.area.inline.TextArea;
import org.jdom.Element;
import org.jdom.Namespace;



public class BitmapPageBuilder extends AbstractPageBuilder {

	private File tempImageDir;

	private PageRenderContext context;

	private float widthMM;

	private float heightMM;

	private boolean renderText;

	private static final Comparator<PageDrawable> zComp = new Comparator<PageDrawable>() {
		@Override
		public int compare(PageDrawable p1, PageDrawable p2) {
			return p1.getZPosition() - p2.getZPosition();
		}
	};

	public BitmapPageBuilder(float widthMM, float heightMM,
			PageRenderContext context, File tempImageDir, boolean renderText) throws IOException {
		this.widthMM = widthMM;
		this.heightMM = heightMM;
		this.context = context;
		this.tempImageDir = tempImageDir;
		this.renderText = renderText;
	}

	@Override
	public void addToDocumentBuilder(XslFoDocumentBuilder docBuilder)
			throws IOException {
		// render drawables onto image, regardless of type
		List<PageDrawable> pageContents = new Vector<PageDrawable>(getDrawables());
		Collections.sort(pageContents, zComp);

		context.getLog().debug("Creating full page image from page elements");

		BufferedImage img = new BufferedImage(context.toPixel(widthMM),
				context.toPixel(heightMM), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
		List<PageText> texts = new ArrayList<PageText>();
		for (PageDrawable pd : pageContents) {
			int left = context.toPixel(pd.getLeftMM());
			int top = context.toPixel(pd.getTopMM());

			Point offset = new Point();
			if(!renderText) {
				if(pd instanceof PageText) {
					texts.add((PageText)pd);
					continue;
				}
			}

			try {
				BufferedImage pdImg = pd.renderAsBitmap(context, offset);
				if (pdImg != null)
					g2d.drawImage(pdImg, left + offset.x, top + offset.y, null);
			}
			catch (FileNotFoundException e) {
				// ignore
				// throw e;
			}

		}

		docBuilder.addPageElement(createXslFoElement(img, docBuilder.getNamespace()), widthMM, heightMM);
		g2d.dispose();

		for(PageText pd : texts) {
			docBuilder.addPageElement(createXslFoElement(pd, docBuilder.getNamespace()), widthMM, heightMM);
		}
	}

	private String toHex(Color textColor) {
	      Integer r = textColor.getRed();
	      Integer g = textColor.getGreen();
	      Integer b = textColor.getBlue();
	      return String.format("#%02x%02x%02x", r, g, b);
	}

	private Element createXslFoElement(PageText pd, Namespace xslFoNs) {
		Element eg = new Element("block-container", xslFoNs);
		McfArea area = pd.getArea();
		float top = pd.getTopMM();
		float left = pd.getLeftMM();
		float width = area.getWidth() / 10f;
		float height = area.getHeight() / 10f;
		if(pd.getRotation() != 0) {
			eg.setAttribute("reference-orientation", pd.getRotation()+"");
		}

		if(area.isBorderEnabled() && area.getBorderColor() != null) {
			eg.setAttribute("border-style", "solid");
			eg.setAttribute("border-color", toHex(area.getBorderColor()));
		}

		if(area.getBackgroundColor() != null) {
			eg.setAttribute("background-color", toHex(area.getBackgroundColor()));
		}

		// now for the text
		eg.setAttribute("absolute-position", "absolute");
		eg.setAttribute("top", top + "mm");
		eg.setAttribute("left", left + "mm");
		eg.setAttribute("width", width + "mm");
		eg.setAttribute("height", height + "mm");
		// blocks
		for(Element block : pd.renderBlocks(xslFoNs))
			eg.addContent(block);

		return eg;
	}

	private Element createXslFoElement(BufferedImage img, Namespace xslFoNs) throws IOException {
		// save bitmap to file
		File f;
		int i = 1;
		do {
			f = new File(tempImageDir, (i++) + ".jpg");
		}
		while (f.isFile());

		BufferedImage imgPlain = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = imgPlain.createGraphics();
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();

		ImageIO.write(imgPlain, "jpeg", f);

		Element eg = new Element("external-graphic", xslFoNs);
		eg.setAttribute("src", extractPath(f));
		eg.setAttribute("content-width", widthMM + "mm");
		eg.setAttribute("content-height", heightMM + "mm");
		f.deleteOnExit();

		return eg;
	}

	private String extractPath(File f) {
		return String.format("file:///%s",f.getAbsolutePath().replace("\\","/"));
	}


}
