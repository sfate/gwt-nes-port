package com.googlecode.gwtnes.client;


/**
 * 
 * Class for the Mapper 06 Controller Konami FFE F4xxx used by NESCafe.
 * 
 * @author David de Niese
 * @author Brad Rydzewski
 * @version 0.56f
 * @final TRUE
 * 
 */
public class Mapper006 extends Mapper {

	/**
	 * 
	 * Irq Counter
	 * 
	 */

	private int irq_counter = 0;

	/**
	 * 
	 * Whether the IRQ has been Set
	 * 
	 */

	private boolean irq_enabled = false;

	/**
	 * 
	 * <P>
	 * Method to initialise the Memory Mapper.
	 * </P>
	 * 
	 * @param mm
	 *            Memory Manager to initialise the Mapper with.
	 * 
	 */

	public void init(MemoryManager mm) {

		// Assign Local Pointer for Memory Manager Object

		this.mm = mm;

		// Cause a Reset

		reset();

	}

	/**
	 * 
	 * <P>
	 * Reset the Mapper.
	 * </P>
	 * 
	 */

	public final void reset() {

		// Set Program ROM Banks

		setCPUBanks(0, 1, 14, 15);

		// Set VROM Banks

		if (getNum1KVROMBanks() > 0) {

			setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);

		} else {

			setVRAMBank(0, 0);
			setVRAMBank(0, 1);
			setVRAMBank(0, 2);
			setVRAMBank(0, 3);
			setVRAMBank(0, 4);
			setVRAMBank(0, 5);
			setVRAMBank(0, 6);
			setVRAMBank(0, 7);

		}

	}

	/**
	 * 
	 * <P>
	 * Access the Mapper in Low Area.
	 * </P>
	 * 
	 */

	public final void access(int addr, int data) {

		// Check Range

		if (addr < 0x8000)
			return;

		// Calculate new Program and VRAM Banks

		int prg_bank = (data & 0x3C) >> 2;
		int chr_bank = data & 0x03;

		// Set Program ROM Banks

		setCPUBank8(prg_bank * 2 + 0);
		setCPUBankA(prg_bank * 2 + 1);

		// Set VRAM Banks in Pattern Tables

		setVRAMBank(0, chr_bank * 8 + 0);
		setVRAMBank(1, chr_bank * 8 + 1);
		setVRAMBank(2, chr_bank * 8 + 2);
		setVRAMBank(3, chr_bank * 8 + 3);
		setVRAMBank(4, chr_bank * 8 + 4);
		setVRAMBank(5, chr_bank * 8 + 5);
		setVRAMBank(6, chr_bank * 8 + 6);
		setVRAMBank(7, chr_bank * 8 + 7);

	}

	/**
	 * 
	 * <P>
	 * Access the Mapper in Low Area.
	 * </P>
	 * 
	 */

	public final void accesslow(int addr, int data) {

		switch (addr) {

		case 0x42FE: {

			if ((data & 0x10) != 0) {

				setMirroringOneScreenHigh();

			} else {

				setMirroringOneScreenLow();

			}

		}
			break;

		case 0x42FF: {

			if ((data & 0x10) != 0) {

				setMirroringHorizontal();

			} else {

				setMirroringVertical();

			}

		}
			break;

		case 0x4501: {

			irq_enabled = false;

		}
			break;

		case 0x4502: {

			// Low Byte of IRQ Counter

			irq_counter = (irq_counter & 0xFF00) | (data & 0xFF);

		}
			break;

		case 0x4503: {

			// High Byte of IRQ Counter

			irq_counter = (irq_counter & 0x00FF) | (data << 8);
			irq_enabled = true;

		}
			break;

		}

	}

	/**
	 * 
	 * <P>
	 * Determine the number of the Memory Mapper.
	 * </P>
	 * 
	 */

	public final int getMapperNumber() {

		return 6;

	}

	/**
	 * 
	 * <P>
	 * Syncronise the Memory Mapper Horizontally.
	 * </P>
	 * 
	 * @return Returns non-zero if a Mapper-specific interrupt occurred.
	 * 
	 */

	public int syncH(int scanline) {

		if (irq_enabled) {

			irq_counter += 133;

			if (irq_counter >= 0xFFFF) {

				irq_counter = 0;
				return 3;

			}

		}

		return 0;

	}

}
