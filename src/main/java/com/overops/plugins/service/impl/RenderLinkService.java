package com.overops.plugins.service.impl;

import com.overops.plugins.service.OutputWriter;
import com.overops.plugins.service.Render;
import org.fusesource.jansi.Ansi;

public class RenderLinkService extends Render
{
	private OutputWriter outputStream;
	private String linkUrl;

	public RenderLinkService(String linkUrl) {
		this.linkUrl = linkUrl;
		outputStream = this.context.getOutputStream();
	}

	@Override
	public Render render()
	{
		outputStream.println("OverOps Quality Report Link", Ansi.Color.WHITE);
		outputStream.println(linkUrl, Ansi.Color.WHITE);
		return null;
	}
}
