package org.mobicents.ss7.management.transceiver;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

import javolution.util.FastList;

public class ChannelSelector {

	protected Selector selector;

	private FastList<ShellSelectionKey> list = new FastList<ShellSelectionKey>();

	protected ChannelSelector(Selector selector) {
		this.selector = selector;
	}

	public static ChannelSelector open() throws IOException {
		return new ChannelSelector(SelectorProvider.provider().openSelector());
	}

	public FastList<ShellSelectionKey> selectNow() throws IOException {
		list.clear();
		selector.selectNow();
		Set<SelectionKey> selection = selector.selectedKeys();
		for (SelectionKey key : selection) {
			if (key.isAcceptable()) {
				ShellSelectionKey k = (ShellSelectionKey) key.attachment();
				list.add(k);
			} else if (key.isReadable()) {
				ShellSelectionKey k = (ShellSelectionKey) key.attachment();
				((ShellChannel) k.channel()).doRead();
				if (k.isReadable()) {
					list.add(k);
				}
			} else {
				ShellSelectionKey k = (ShellSelectionKey) key.attachment();
				((ShellChannel) k.channel()).doWrite();
				if (k.isWritable()) {
					list.add(k);
				}
			}
		}
		selection.clear();
		return list;
	}

	public void close() throws IOException {
		selector.close();
	}
}