#![allow(clippy::unusual_byte_groupings)]
use riscv::singleton;
use volatile_register::*;

const PIO_ADDR: usize = 0x6004_0000;

pub struct Pio(&'static mut PioRegisters);

#[repr(C)]
struct PioRegisters {
    instructions: [WO<u32>; 32],
    clock_div: WO<u32>,
    branch_pin: WO<u32>,
    wrap_config: WO<u32>,
    input_pin_config: WO<u32>,
    output_pin_config: WO<u32>,
    isr_config: WO<u32>,
    osr_config: WO<u32>,
    enable: WO<u32>,
}

impl Pio {
    pub unsafe fn new() -> Self {
        Pio((PIO_ADDR as *mut PioRegisters).as_mut().unwrap())
    }

    pub fn take() -> Option<Self> {
        singleton!(:&'static mut PioRegisters = unsafe{(PIO_ADDR as *mut PioRegisters).as_mut()?})
            .map(|regs| Pio(regs))
    }

    pub fn enable(&mut self) {
        unsafe {
            self.0.enable.write(1);
        }
    }

    pub fn blink(&mut self) {
        let program: [u32; 3] = [
            0b111_0_0001_110_00001,
            0b111_0_0000_110_00000,
            0b000_0_0000_000_00000,
        ];
        unsafe {
            for (a, i) in program.iter().enumerate() {
                self.0.instructions[a].write(*i);
            }
            self.0.clock_div.write((u16::MAX - 1) as u32);
            self.0.output_pin_config.write(0b0000_0001_0000_0000);
        }

        self.enable();
    }
}

pub fn LEDs(pio: &mut Pio) {
    let program: [u32; 3] = [
        0b011_0_0010_001_00001, //shift 1 bit from OSR into X, side set 0, delay 2
        0b000_1_0010_001_00000, //branch on value of X (if x is zero then jump to 0, else fall through to 2), side 1 delay 2
        0b000_1_0100_000_00000, //jump back to 0, side 1 delay 4
    ];
    unsafe {
        for (a, i) in program.iter().enumerate() {
            pio.0.instructions[a].write(*i);
        }
        //clock divider 0 (original clock (10MHz))
        pio.0.wrap_config.write(0b1_00000_00011_00000);
        pio.0.osr_config.write(0b_11000_1_1);
    }
}
