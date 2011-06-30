package com.googlecode.gwtnes.client.halfnes;
//HalfNES, Copyright Andrew Hoffman, October 2010


//where doin it man
//where making this happen
public final class CPU {

    private final CPURAM ram;
    public int cycles;
    private int A, X, Y, S;
    private boolean carryFlag = false;
    private boolean zeroFlag = false;
    private boolean interruptsDisabled = true;
    private boolean decimalModeFlag = false;
    private boolean breakFlag = true;
    // break flag doesn't actually do anything, but incl.anyways
    private boolean overflowFlag = false;
    private boolean negativeFlag = false;
    public int PC; // registers
    private int pb = 0;// set to 1 if access crosses page boundary
    // todo: do that
    public int interrupt = 0;
    private final static int ntscframe = 29780;
    private final static boolean logging = false;
    //FileWriter w; //debug log writer

    public CPU(final CPURAM cpuram) {
        ram = cpuram;
        //ram is the ONLY thing the cpu tries to talk to.

    }

    public void init() {// different than reset
        // puts RAM in NES poweron state
        for (int i = 0; i < 0x800; ++i) {
            ram.write(i, 0xFF);
        }

        ram.write(0x0008, 0xF7);
        ram.write(0x0009, 0xEF);
        ram.write(0x000A, 0xDF);
        ram.write(0x000F, 0xBF);

        for (int i = 0x4000; i <= 0x4017; ++i) {
            ram.write(i, 0x00);
        }
        ram.write(0x4017, 0x00);
        ram.write(0x4015, 0x00);

        cycles = 27394; //correct for position we start vblank in
        A = 0x00;
        X = 0;
        Y = 0;
        S = 0xFD;
        PC = ram.read(0xFFFD) * 256 + ram.read(0xFFFC);
    }

    public void reset() {
        PC = ram.read(0xFFFD) * 256 + ram.read(0xFFFC);
        ram.write(0x4015, 0);
        //disable audio on reset
        S -= 3;
        S &= 0xff;
        interruptsDisabled = true;
    }

    public void cycle(final int scanline, final int endpixel) {

        while ((cycles * 3) < ((scanline * 341) + endpixel)) {
            runcycle(scanline, (cycles * 3) - (scanline * 341));
        }

    }

    public void runcycle(final int scanline, final int pixel) {
        if (interrupt > 0 && !interruptsDisabled) {
            interrupt();
            cycles += 7;
            return;
        }
        pb = 0;
        final int instr = ram.read(PC++);

        switch (instr) {
            // ADC
            case 0x69:
                adc(imm());
                cycles += 2;
                break;
            case 0x65:
                adc(zpg());
                cycles += 3;
                break;
            case 0x75:
                adc(zpg(X));
                cycles += 4;
                break;
            case 0x6d:
                adc(abs());
                cycles += 4;
                break;
            case 0x7d:
                adc(abs(X));
                cycles += 4 + pb;
                break;
            case 0x79:
                adc(abs(Y));
                cycles += 4 + pb;
                break;
            case 0x61:
                adc(indX());
                cycles += 6;
                break;
            case 0x71:
                adc(indY());
                cycles += 5 + pb;
                break;
            // AND
            case 0x29:
                and(imm());
                cycles += 2;
                break;
            case 0x25:
                and(zpg());
                cycles += 2;
                break;
            case 0x35:
                and(zpg(X));
                cycles += 3;
                break;
            case 0x2D:
                and(abs());
                cycles += 4 + pb;
                break;
            case 0x3D:
                and(abs(X));
                cycles += 4 + pb;
                break;
            case 0x39:
                and(abs(Y));
                cycles += 4 + pb;
                break;
            case 0x21:
                and(indX());
                cycles += 6;
                break;
            case 0x31:
                and(indY());
                cycles += 5 + pb;
                break;
            // ASL
            case 0x0A:
                aslA();
                cycles += 2;
                break;
            case 0x06:
                asl(zpg());
                cycles += 5;
                break;
            case 0x16:
                asl(zpg(X));
                cycles += 6;
                break;
            case 0x0e:
                asl(abs());
                cycles += 6;
                break;
            case 0x1e:
                asl(abs(X));
                cycles += 7;
                break;
            // BIT
            case 0x24:
                bit(zpg());
                cycles += 3;
                break;
            case 0x2c:
                bit(abs());
                cycles += 4;
                break;
            // Branches: every branch uses rel. addressing
            case 0x10:
                branch(!negativeFlag);
                cycles += 2 + pb;
                break;
            case 0x30:
                branch(negativeFlag);
                cycles += 2 + pb;
                break;
            case 0x50:
                branch(!overflowFlag);
                cycles += 2 + pb;
                break;
            case 0x70:
                branch(overflowFlag);
                cycles += 2 + pb;
                break;
            case 0x90:
                branch(!carryFlag);
                cycles += 2 + pb;
                break;
            case 0xB0:
                branch(carryFlag);
                cycles += 2 + pb;
                break;
            case 0xD0:
                branch(!zeroFlag);
                cycles += 2 + pb;
                break;
            // it keeps happening
            case 0xF0:
                branch(zeroFlag);
                cycles += 2 + pb;
                break;
            // BRK
            case 0x00:
                breakinterrupt();
                cycles += 7;
                break;
            // CMP
            case 0xc9:
                cmp(A, imm());
                cycles += 2;
                break;
            case 0xc5:
                cmp(A, zpg());
                cycles += 3;
                break;
            case 0xd5:
                cmp(A, zpg(X));
                cycles += 4;
                break;
            case 0xcd:
                cmp(A, abs());
                cycles += 4;
                break;
            case 0xdd:
                cmp(A, abs(X));
                cycles += 4 + pb;
                break;
            case 0xd9:
                cmp(A, abs(Y));
                cycles += 4 + pb;
                break;
            case 0xc1:
                cmp(A, indX());
                cycles += 6;
                break;
            case 0xd1:
                cmp(A, indY());
                cycles += 5 + pb;
                break;
            // CPX
            case 0xe0:
                cmp(X, imm());
                cycles += 2;
                break;
            case 0xe4:
                cmp(X, zpg());
                cycles += 3;
                break;
            case 0xec:
                cmp(X, abs());
                cycles += 4;
                break;
            // CPY
            case 0xc0:
                cmp(Y, imm());
                cycles += 2;
                break;
            case 0xc4:
                cmp(Y, zpg());
                cycles += 3;
                break;
            case 0xcc:
                cmp(Y, abs());
                cycles += 4;
                break;
            // DEC
            case 0xc6:
                dec(zpg());
                cycles += 5;
                break;
            case 0xd6:
                dec(zpg(X));
                cycles += 6;
                break;
            case 0xce:
                dec(abs());
                cycles += 6;
                break;
            case 0xde:
                dec(abs(X));
                cycles += 7;
                break;
            // EOR
            case 0x49:
                eor(imm());
                cycles += 2;
                break;
            case 0x45:
                eor(zpg());
                cycles += 3;
                break;
            case 0x55:
                eor(zpg(X));
                cycles += 4;
                break;
            case 0x4d:
                eor(abs());
                cycles += 4;
                break;
            case 0x5d:
                eor(abs(X));
                cycles += 4 + pb;
                break;
            case 0x59:
                eor(abs(Y));
                cycles += 4 + pb;
                break;
            case 0x41:
                eor(indX());
                cycles += 6;
                break;
            case 0x51:
                eor(indY());
                cycles += 5;
                break;
            // Flag set/clear
            case 0x18:
                carryFlag = false;
                cycles += 2;
                break;
            case 0x38:
                carryFlag = true;
                cycles += 2;
                break;
            case 0x58:
                interruptsDisabled = false;
                cycles += 2;
                break;
            case 0x78:
                interruptsDisabled = true;
                cycles += 2;
                break;
            case 0xb8:
                overflowFlag = false;
                cycles += 2;
                break;
            case 0xd8:
                decimalModeFlag = false;
                cycles += 2;
                break;// decimal mode doesnt
            case 0xf8:
                decimalModeFlag = true;
                cycles += 2;
                break;// do anything on NES
            // INC
            case 0xe6:
                inc(zpg());
                cycles += 5;
                break;
            case 0xf6:
                inc(zpg(X));
                cycles += 6;
                break;
            case 0xee:
                inc(abs());
                cycles += 6;
                break;
            case 0xfe:
                inc(abs(X));
                cycles += 7;
                break;
            // JMP
            case 0x4c:
                PC = abs();
                cycles += 3;
                break;
            case 0x6c:
                PC = ind();
                cycles += 5;
                break;
            // JSR
            case 0x20:
                jsr(abs());
                cycles += 6;
                break;
            // LAX (illegal; adding for nestest)
            case 0xa3:
                lax(indX());
                break;
            case 0xb3:
                lax(indY());
                break;
            case 0x83:
                lax(abs(Y));
                break;
            case 0xa7:
                lax(zpg());
                break;
            case 0xb7:
                lax(zpg(Y));
                break;
            case 0xaf:
                lax(abs());
                break;
            case 0xbf:
                lax(abs(Y));
                break;
            // LDA
            case 0xa9:
                lda(imm());
                cycles += 2;
                break;
            case 0xa5:
                lda(zpg());
                cycles += 3;
                break;
            case 0xb5:
                lda(zpg(X));
                cycles += 4;
                break;
            case 0xad:
                lda(abs());
                cycles += 4;
                break;
            case 0xbd:
                lda(abs(X));
                cycles += 4 + pb;
                break;
            case 0xb9:
                lda(abs(Y));
                cycles += 4 + pb;
                break;
            case 0xa1:
                lda(indX());
                cycles += 6;
                break;
            case 0xb1:
                lda(indY());
                cycles += 5 + pb;
                break;
            // LDX
            case 0xa2:
                ldx(imm());
                cycles += 2;
                break;
            case 0xa6:
                ldx(zpg());
                cycles += 3;
                break;
            case 0xb6:
                ldx(zpg(Y));
                cycles += 4;
                break;
            case 0xae:
                ldx(abs());
                cycles += 4;
                break;
            case 0xbe:
                ldx(abs(Y));
                cycles += 4 + pb;
                break;
            // LDY
            case 0xa0:
                ldy(imm());
                cycles += 2;
                break;
            case 0xa4:
                ldy(zpg());
                cycles += 3;
                break;
            case 0xb4:
                ldy(zpg(X));
                cycles += 4;
                break;
            case 0xac:
                ldy(abs());
                cycles += 4;
                break;
            case 0xbc:
                ldy(abs(X));
                cycles += 4 + pb;
                break;
            // LSR
            case 0x4a:
                lsrA();
                cycles += 2;
                break;
            case 0x46:
                lsr(zpg());
                cycles += 5;
                break;
            case 0x56:
                lsr(zpg(X));
                cycles += 6;
                break;
            case 0x4e:
                lsr(abs());
                cycles += 6;
                break;
            case 0x5e:
                lsr(abs(X));
                cycles += 7;
                break;
            // NOP
            case 0x1a:
            case 0x3a:
            case 0x5a:
            case 0x7a:
            case 0xda:
            case 0xEA:
            case 0xfa:
                cycles += 2;
                break;
            case 0x80:
            case 0x04:
                PC++;
                cycles += 3;// ?
                break;
            case 0x14:
            case 0x34:
            case 0x44:
            case 0x54:
            case 0x64:
            case 0x74:
            case 0xd4:
            case 0xf4:
                PC++;
                cycles += 4;// ?
                break;
            case 0x0C:
            case 0x3c:
            case 0x5c:
            case 0x7c:
            case 0xdc:
            case 0xfc:
                PC += 2;
                cycles += 4;// ?
                break;
            case 0x1c:
                PC += 2;
                //abs(X);
                cycles += 4 + pb;
                break;
            // TODO add the other multi-byte nops
            // ORA
            case 0x09:
                ora(imm());
                cycles += 2;
                break;
            case 0x05:
                ora(zpg());
                cycles += 3;
                break;
            case 0x15:
                ora(zpg(X));
                cycles += 4;
                break;
            case 0x0d:
                ora(abs());
                cycles += 4;
                break;
            case 0x1d:
                ora(abs(X));
                cycles += 4 + pb;
                break;
            case 0x19:
                ora(abs(Y));
                cycles += 4 + pb;
                break;
            case 0x01:
                ora(indX());
                cycles += 6;
                break;
            case 0x11:
                ora(indY());
                cycles += 5 + pb;
                break;
            // Register instrs.
            case 0xAA:
                X = A;
                cycles += 2;
                setflags(A);
                break;
            case 0x8a:
                A = X;
                cycles += 2;
                setflags(A);
                break;
            case 0xca:
                X--;
                X &= 0xFF;
                setflags(X);
                cycles += 2;
                break;
            case 0xe8:
                X++;
                X &= 0xFF;
                setflags(X);
                cycles += 2;
                break;
            case 0xa8:
                Y = A;
                cycles += 2;
                setflags(A);
                break;
            case 0x98:
                A = Y;
                cycles += 2;
                setflags(A);
                break;
            case 0x88:
                Y--;
                Y &= 0xFF;
                setflags(Y);
                cycles += 2;
                break;
            case 0xc8:
                Y++;
                Y &= 0xFF;
                setflags(Y);
                cycles += 2;
                break;
            // ROL
            case 0x2a:
                rolA();
                cycles += 2;
                break;
            case 0x26:
                rol(zpg());
                cycles += 5;
                break;
            case 0x36:
                rol(zpg(X));
                cycles += 6;
                break;
            case 0x2e:
                rol(abs());
                cycles += 6;
                break;
            case 0x3e:
                rol(abs(X));
                cycles += 6;
                break;
            // ROR
            case 0x6a:
                rorA();
                cycles += 2;
                break;
            case 0x66:
                ror(zpg());
                cycles += 5;
                break;
            case 0x76:
                ror(zpg(X));
                cycles += 6;
                break;
            case 0x6e:
                ror(abs());
                cycles += 6;
                break;
            case 0x7e:
                ror(abs(X));
                cycles += 6;
                break;
            // RTI
            case 0x40:
                rti();
                cycles += 6;
                break;
            // RTS
            case 0x60:
                rts();
                cycles += 6;
                break;
            // SBC
            case 0xE9:
                sbc(imm());
                cycles += 2;
                break;
            case 0xE5:
                sbc(zpg());
                cycles += 3;
                break;
            case 0xF5:
                sbc(zpg(X));
                cycles += 4;
                break;
            case 0xEd:
                sbc(abs());
                cycles += 4;
                break;
            case 0xFd:
                sbc(abs(X));
                cycles += 4 + pb;
                break;
            case 0xF9:
                sbc(abs(Y));
                cycles += 4 + pb;
                break;
            case 0xE1:
                sbc(indX());
                cycles += 6;
                break;
            case 0xF1:
                sbc(indY());
                cycles += 5 + pb;
                break;
            // STA
            case 0x85:
                sta(zpg());
                cycles += 3;
                break;
            case 0x95:
                sta(zpg(X));
                cycles += 4;
                break;
            case 0x8d:
                sta(abs());
                cycles += 4;
                break;
            case 0x9d:
                sta(abs(X));
                cycles += 5;
                break;
            case 0x99:
                sta(abs(Y));
                cycles += 5;
                break;
            case 0x81:
                sta(indX());
                cycles += 6;
                break;
            case 0x91:
                sta(indY());
                cycles += 6;
                break;
            // Stack instructions
            case 0x9A:
                S = X;
                cycles += 2;
                break;
            case 0xBA:
                X = S;
                cycles += 2;
                setflags(X);
                break;
            case 0x48:
                push(A);
                cycles += 3;
                break;
            case 0x68:
                A = pop();
                setflags(A);
                cycles += 4;
                break;
            case 0x08:
                push(flagstobyte() | 0x30);
                cycles += 3;
                break;
            case 0x28:
                bytetoflags(pop());
                cycles += 4;
                break;
            // STX
            case 0x86:
                stx(zpg());
                cycles += 3;
                break;
            case 0x96:
                stx(zpg(Y));
                cycles += 4;
                break;
            case 0x8E:
                stx(abs());
                cycles += 4;
                break;
            // STY
            case 0x84:
                sty(zpg());
                cycles += 3;
                break;
            case 0x94:
                sty(zpg(X));
                cycles += 4;
                break;
            case 0x8c:
                sty(abs());
                cycles += 4;
                break;
            default:
                cycles += 2;
                System.err.println("Illegal opcode:" + Utils.hex(instr) + " @ "
                        + Utils.hex(PC - 1));
                break;
        }
        pb = 0;
        PC &= 0xffff;
    }

    private void lax(final int data) {
        A = data;
        X = data;
        setflags(data);
    }

    private void rol(final int addr) {
        int data = (ram.read(addr) << 1) | (carryFlag ? 1 : 0);
        carryFlag = Utils.getbit(data, 8);
        data &= 0xFF;
        setflags(data);
        ram.write(addr, data);
    }

    private void rolA() {
        A = A << 1 | (carryFlag ? 1 : 0);
        carryFlag = Utils.getbit(A, 8);
        A &= 0xFF;
        setflags(A);
    }

    private void ror(final int addr) {
        int data = ram.read(addr);
        final boolean tmp = carryFlag;
        carryFlag = Utils.getbit(data, 0);
        data >>= 1;
        data &= 0x7F;
        data |= (tmp ? 0x80 : 0);
        setflags(data);
        ram.write(addr, data);
    }

    private void rorA() {
        final boolean tmp = carryFlag;
        carryFlag = Utils.getbit(A, 0);
        A >>= 1;
        A &= 0x7F;
        A |= (tmp ? 128 : 0);
        setflags(A);
    }

    public void nmi() {

        push(PC >> 8); // high bit 1st
        push((PC) & 0xFF);// check that this pushes right address
        push(flagstobyte());
        PC = ram.read(0xFFFA) + (ram.read(0xFFFB) << 8);
        cycles += 7;
        interruptsDisabled = true;
    }

    private void interrupt() {

        push(PC >> 8); // high bit 1st
        push(PC & 0xFF);// check that this pushes right address
        push(flagstobyte());
        //jump to reset vector
        PC = ram.read(0xFFFE) + (ram.read(0xFFFF) << 8);
        interruptsDisabled = true;
    }

    private void breakinterrupt() {
        //same as interrupt but BRK flag is turned on

        PC++;
        push(PC >> 8); // high bit 1st
        push(PC & 0xFF);// check that this pushes right address
        push(flagstobyte() | 0x30);//push byte w/bits 4+5 set
        PC = ram.read(0xFFFE) + (ram.read(0xFFFF) << 8);
        interruptsDisabled = true;
    }

    private void lsr(final int addr) {
        int data = ram.read(addr);
        carryFlag = Utils.getbit(data, 0);
        data >>= 1;
        data &= 0x7F;
        setflags(data);
        ram.write(addr, data);
    }

    private void lsrA() {
        carryFlag = Utils.getbit(A, 0);
        A >>= 1;
        A &= 0x7F;
        setflags(A);
    }

    private void eor(final int addr) {
        A ^= ram.read(addr);
        A &= 0xff;
        setflags(A);
    }

    private void ora(final int addr) {
        A |= ram.read(addr);
        A &= 0xff;
        setflags(A);
    }

    // Instructions
    private void bit(final int addr) {
        final int data = ram.read(addr);
        zeroFlag = ((data & A) == 0);
        negativeFlag = Utils.getbit(data, 7);
        overflowFlag = Utils.getbit(data, 6);
    }

    private void jsr(final int addr) {
        PC--;
        push(PC >> 8); // high bit 1st
        push(PC & 0xFF);// check that this pushes right address
        PC = addr;
    }

    private void rts() {
        PC = (pop() & 0xff) + (pop() << 8);// page crossing bug again?
        PC++;
    }

    private void rti() {
        bytetoflags(pop());
        PC = ((pop()) & 0xff) + (pop() << 8); // not plus one
    }

    private int pop() {
        ++S;
        S &= 0xff;
        return ram.read(0x100 + S);
    }

    private void push(final int byteToPush) {
        ram.write((0x100 + (S & 0xff)), byteToPush);
        --S;
        S &= 0xff;
    }

    private void branch(final boolean isTaken) {
        if (isTaken) {
            final int pcprev = PC + 1;// store prev. PC
            PC = rel();
            cycles++;
            // System.err.println(pcprev + " "+ PC);
            //page boundary penalty
            if ((pcprev >> 8) != (PC >> 8)) {
                pb = 2;//page crossing for branch takes 2 ctcles
            }
        } else {
            rel();
            // have to do the memory access even if we're not branching
        }
    }

    private void inc(final int addr) {
        int tmp = ram.read(addr);
        ram.write(addr, tmp);
        //dummy write
        ++tmp;
        tmp &= 0xff;
        ram.write(addr, tmp);
        //THEN real write
        setflags(tmp);
    }

    private void dec(final int addr) {
        int tmp = ram.read(addr);
        ram.write(addr, tmp);
        //dummy write
        --tmp;
        tmp &= 0xff;
        ram.write(addr, tmp);
        //THEN real write
        setflags(tmp);
    }

    private void adc(final int addr) {
        final int value = ram.read(addr);
        final int result = value + A + (carryFlag ? 1 : 0);
        carryFlag = (result >> 8 != 0);
        // set overflow flag
        overflowFlag = (((A ^ value) & 0x80) == 0)
                && (((A ^ result) & 0x80) != 0);
        A = result & 0xff;
        setflags(A);// set other flags
    }

    private void sbc(final int addr) {
        final int value = ram.read(addr);
        final int result = A - value - (carryFlag ? 0 : 1);
        carryFlag = (result >> 8 == 0);
        // set overflow flag
        overflowFlag = (((A ^ value) & 0x80) != 0)
                && (((A ^ result) & 0x80) != 0);
        A = result & 0xff;
        setflags(A);// set other flags

    }

    private void and(final int addr) {
        A &= ram.read(addr);
        setflags(A);
    }

    private void asl(final int addr) {
        int data = ram.read(addr);
        carryFlag = Utils.getbit(data, 7);
        data = data << 1;
        data &= 0xff;
        setflags(data);
        ram.write(addr, data);
    }

    private void aslA() {
        carryFlag = Utils.getbit(A, 7);
        A <<= 1;
        A &= 0xff;
        setflags(A);

    }

    private void cmp(final int regval, final int addr) {
        final int comparisonVal = ram.read(addr);
        final int result = regval - comparisonVal;
        if (result < 0) {
            negativeFlag = Utils.getbit(result, 7);
            carryFlag = false;
            zeroFlag = false;
        } else if (result == 0) {
            negativeFlag = false;
            carryFlag = true;
            zeroFlag = true;
        } else {
            negativeFlag = Utils.getbit(result, 7);
            carryFlag = true;
            zeroFlag = false;
        }
    }

    private void lda(final int addr) {
        A = ram.read(addr);
        setflags(A);
    }

    private void ldx(final int addr) {
        X = ram.read(addr);
        setflags(X);
    }

    private void ldy(final int addr) {
        Y = ram.read(addr);
        setflags(Y);
    }

    private void setflags(final int result) {
        zeroFlag = (result == 0);
        negativeFlag = Utils.getbit(result, 7);
    }

    private void sta(final int addr) {
        ram.write(addr, A);
    }

    private void stx(final int addr) {
        ram.write(addr, X);
    }

    private void sty(final int addr) {
        ram.write(addr, Y);
    }

    // Functions for memory address types; each returns the _memory_address_ for
    // the next fn
    protected int imm() {
        return PC++;
    }

    protected int zpg() {
        // zero page mode
        return ram.read(PC++);
    }

    protected int zpg(final int reg) {
        // zero page added to register (modulus page boundary)
        return (ram.read(PC++) + reg) & 0xff;
    }

    protected int rel() {
        // returns actual value of PC, not memory location to look at
        // because only branches use this
        return ((byte) ram.read(PC++)) + PC;
    }

    protected int abs() {
        // absolute mode
        return ram.read(PC++) + (ram.read(PC++) << 8);
    }

    protected int abs(final int reg) {
        // absolute plus value from reg
        final int first = ram.read(PC++) + reg;
        //page crossing cycle penalty
        if (first > 0xff) {
            pb = 1;
        }
        return (first + (ram.read(PC++) << 8)) & 0xffff;
    }

    protected int ind() {
        // weird mode. only used by jmp
        final int readloc = abs();
        return ram.read(readloc)
                + (ram.read(((readloc & 0xff) == 0xff) ? readloc - 0xff
                : readloc + 1) << 8);
        //if reading from the last byte in a page, high bit of address
        //is taken from first byte on the page, not first byte on NEXT page.
    }

    protected int indX() {
        // indirect mode
        final int arg = ram.read(PC++);
        return ram.read((arg + X) & 0xff)
                + (ram.read((arg + 1 + X) & 0xff) << 8);
        // doesn't suffer from the same bug as jump indirect
    }

    protected int indY() {
        final int arg = ram.read(PC++);
        // System.err.println("#" + utils.hex(arg));
        final int addr = ram.read(arg) + (ram.read((arg + 1) & 0xff) << 8);
        if (addr >> 8 != (addr + Y) >> 8) {
            pb = 1;
        }
        return addr + Y;
    }

    public int flagstobyte() {
        return ((negativeFlag ? 128 : 0)
                + (overflowFlag ? 64 : 0)
                + (breakFlag ? 32 : 0)
                + (decimalModeFlag ? 8 : 0) + (interruptsDisabled ? 4 : 0)
                + (zeroFlag ? 2 : 0)
                + (carryFlag ? 1 : 0));
    }

    protected void bytetoflags(final int statusbyte) {

        negativeFlag = ((statusbyte & 128) != 0);
        overflowFlag = ((statusbyte & 64) != 0);
        //breakFlag = ((b & 32) != 0);
        // unusedFlag = ((b & 16) != 0);
        // actually nestest wants the unused flag to always be zero,
        // and doesn't set the break flag with a plp
        decimalModeFlag = ((statusbyte & 8) != 0);
        interruptsDisabled = ((statusbyte & 4) != 0);
        zeroFlag = ((statusbyte & 2) != 0);
        carryFlag = ((statusbyte & 1) != 0);

    }

    public String status() {
        return " PC:" + Utils.hex(PC) + " A:" + Utils.hex(A) + " X:"
                + Utils.hex(X) + " Y:" + Utils.hex(Y) + " P:"
                + Utils.hex(flagstobyte()) + " SP:" + Utils.hex(S);
    }

    public void modcycles() {
        cycles %= ntscframe;
    }
}
