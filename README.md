# RISC-V PIO

This is my third year project and the subject of [my dissertation](https://github.com/joeyh021/cs351-project), programmable state-machine based I/O blocks designed for use in low-power RISC-V SoCs.

## Usage

- `sbt run` generates the verilog, which can then be synthesised or simulated however you please
- `sbt test` runs the testbenches, verifying things work as intended
  - Verilator is required to run some of the tests due to the included Verilog files.

## Including within a Vivado block design

1. Run `sbt run` to write the Verilog to `gen/PioAxiWrapper.v`
2. In Vivado click 'add design sources' and import the generated Verilog file
3. Create a new block design if you don't have one already. The one we used as a base and recommend is https://github.com/eugene-tarassov/vivado-risc-v
4. On the block design, right-click -> add module -> `PioAxiWrapper.v`
5. Connect the AXI slave to a master within your block design

## Programming the PIO

The primary reference for using the PIO is [The RP2040 Datasheet](https://datasheets.raspberrypi.com/rp2040/rp2040-datasheet.pdf). `/docs` includes some rough documentation on the instruction set as implemented by this hardware. [My dissertation](https://github.com/joeyh021/cs351-project) also serves as good documentation.

## Building the driver software

A simple Rust test binary is included in `/drivers` . The [`justfile`](https://github.com/casey/just) includes a recipe to build it: `just binary`. `drivers/src/pio.rs` contains a driver interface for the PIO. If you wish to use the PIO within a larger piece of software, the types and methods exported by this module will be of use.
