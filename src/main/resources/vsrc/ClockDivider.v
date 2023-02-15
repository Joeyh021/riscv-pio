//simple clock divider since firrtl wOnt EmiT cLoCk mUx
module ClockDivider(
    input clock, reset,
    input [16:0] divisor,
    output reg outClk, outRst
);

    reg [16:0] counter;

    always @ (posedge clock) begin
        if (reset) begin
            counter <= 16'b0;
            outClk <= 1'b1;
            outRst <= 1'b1;
        end else if (counter == 0) begin
            counter <= divisor - 16'b1;
            outClk <= 1'b1;
            outRst <= 1'b0;
        end else begin
            counter <= counter - 16'b1;
            outClk <= 1'b0;
            outRst <= 1'b0;
        end
    end

endmodule