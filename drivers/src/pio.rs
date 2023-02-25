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

    #[allow(clippy::unusual_byte_groupings)]
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
            self.0.clock_div.write((u16::MAX - 5) as u32);
            self.0.output_pin_config.write(0b0000_0001_0000_0000);
        }

        self.enable();
    }
}
