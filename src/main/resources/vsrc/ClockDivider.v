//simple clock divider since firrtl wOnt EmiT cLoCk mUx
module ClockDivider(
    input clock,
    input [15:0] divisor,
    output reg outClk
);

    reg [15:0] counter;
    reg [15:0] divisor_reg;
    wire reset = (divisor != divisor_reg);

    always @ (posedge clock) begin
        if (reset) begin
            counter <= 16'b0;
            outClk <= 1'b1;
            divisor_reg <= divisor;
        end else if (counter == divisor_reg - 16'b1) begin
            counter <= 16'b0;
            outClk <= 1'b1;
        end else begin
            counter <= counter + 16'b1;
            outClk <= 1'b0;
        end
    end

endmodule