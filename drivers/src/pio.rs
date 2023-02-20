use riscv::singleton;
use volatile_register::*;

const PIO_ADDR: usize = 0x6004_0000;

pub struct Pio(&'static mut PioRegisters);

#[repr(C)]
struct PioRegisters {
    instructions: RW<[u16; 32]>,
}

impl Pio {
    pub unsafe fn new() -> Self {
        Pio((PIO_ADDR as *mut PioRegisters).as_mut().unwrap())
    }

    pub fn take() -> Option<Self> {
        singleton!(:&'static mut PioRegisters = unsafe{(PIO_ADDR as *mut PioRegisters).as_mut()?})
            .map(|regs| Pio(regs))
    }
}
