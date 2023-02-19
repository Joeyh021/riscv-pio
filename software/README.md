# Rust Seven Segment Display Example

A really basic example of running a bare metal RISC-V Rust program on an FPGA, using a [Rocket Chip](https://github.com/chipsalliance/rocket-chip) SoC genererated by <https://github.com/eugene-tarassov/vivado-risc-v>. Included are basic drivers for the Serial/UART device, and for a seven segment display device I wrote.

The 7 segment display on the [Nexys A7 board](https://digilent.com/shop/nexys-a7-fpga-trainer-board-recommended-for-ece-curriculum/) has 8 digits, so the I/O device contains 8 memory mapped registers connected over an AXI-Lite bus. The display will show any hex digit (0-9, A-F). The example included just scrolls through the digits 0-9, writing them both to the display and to the serial output.

I failed to get rustc/lld to generate me an executable that would run on the FPGA, so I just resorted to compiling the Rust crate to a static library, then linking with the GCC cross compiler instead, using the link script, makefile, and startup assembly code from <https://github.com/eugene-tarassov/vivado-risc-v/tree/master/bare-metal>. Credit to Eugene Tarassov for that, and for the rest of that incredibly useful repo.
