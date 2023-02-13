module Pins(
    input [8:0] cfg_inBase, cfg_inCount, cfg_outBase, cfg_outCount,
    output [31:0] read,
    input [31:0] write_data,
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
    //generate 32 iobufs
    for(genvar i = 0; i < 10; i = i+1) begin
        assign pins[i] = outputEnables[i] ? outputData[i] : 1'bz;
        assign inputData[i] = pins[i];
    end


endmodule