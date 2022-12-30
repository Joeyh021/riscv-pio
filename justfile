gen:
    sbt "runMain dev.joeyh.Main"    
    rm gen/*.fir gen/*.anno.json

test:
    sbt test

clean:
    rm -rf gen/*