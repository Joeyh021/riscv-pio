#![no_std]

mod pio;
mod serial;

use embedded_hal::blocking::delay::*;
use riscv::delay::McycleDelay;
use serial::Serial;

#[no_mangle]
pub extern "C" fn main() -> ! {
    let mut serial = Serial::take().unwrap();
    let mut delay = McycleDelay::new(50_000_000);

    loop {
        serial.print("\n");
        serial.println("Hello from Rust!");
        delay.delay_ms(100_u8);
    }
}

#[panic_handler]
fn panic(info: &core::panic::PanicInfo) -> ! {
    let mut serial = unsafe { Serial::new() };
    if let Some(s) = info.payload().downcast_ref::<&str>() {
        serial.print("panic occurred:");
        serial.print(s);
        serial.write_byte(b'\n');
    } else {
        serial.println("panic occurred");
    }
    loop {}
}
