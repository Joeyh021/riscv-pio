module Pins(
    input [8:0] cfg_inBase, cfg_inCount, cfg_outBase, cfg_outCount,
    output [31:0] read,
    input [31:0] write_data,
    input sideSet,
    input write_enable,
    inout [31:0] pins
);

    wire [31:0] inMask  = (32'b1 << cfg_inCount)  - 32'b1;
    wire [31:0] outMask = (32'b1 << cfg_outCount) - 32'b1;

    wire [31:0] inputData;
    reg  [31:0] outputData;

    assign read = (inputData >> cfg_inBase) & inMask;

    //WE WANT THIS TO LATCH
    always @ *
        if(write_enable)
            outputData = (write_data & outMask) << cfg_outBase;

    wire [31:0] outputEnables = outMask << cfg_outBase;

    //generate THIRTY ONE iobufs
    for(genvar i = 0; i < 31; i = i+1) begin
        //use Xilinx's IOBUF
        IOBUF #( 
            .DRIVE(12), // Specify the output drive strength
            .IBUF_LOW_PWR("TRUE"),  // Low Power - "TRUE", High Performance = "FALSE"
            .IOSTANDARD("LVCMOS33"), // IO standard from constraints
            .SLEW("SLOW") // Specify the output slew rate
        ) IOBUF_inst (
            .O(inputData[i]),     // Buffer output
            .IO(pins[i]),   // Buffer inout port (connect directly to top-level port)
            .I(outputData[i]),     // Buffer input
            .T(~outputEnables[i])      // 3-state enable input, high=input, low=output
        );

    end

    //32nd output is always side set
    assign pins[31] = sideSet;


endmodule