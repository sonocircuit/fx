FxSetup {
    classvar <sendA, <sendB, <wet, <fxGroup, <sendAGroup, <sendBGroup, <insertGroup, plugins, initOnce;

    *alloc {
        if(initOnce.isNil, {
            initOnce = true;
            sendA = Bus.audio(Server.default, numChannels: 2);
            sendB = Bus.audio(Server.default, numChannels: 2);
            wet = Bus.audio(Server.default, numChannels: 2);
            ~sendA = sendA;
            ~sendB = sendB;
			"INIT THIS ONCE YO".postln;
        })
    }

    *dynamicInit {
        if(fxGroup.isNil, {
            fxGroup = Group.new(Server.default, addAction: \addToTail);
            sendAGroup = Group.new(fxGroup, addAction: \addToTail);
            sendBGroup = Group.new(fxGroup, addAction: \addToTail);
            insertGroup = Group.new(fxGroup, addAction: \addToTail);
			"FX setup complete".postln;
        })
    }

    *dynamicCleanup {
        if(fxGroup.notNil, {
            fxGroup.free;
            fxGroup = nil;
            sendAGroup = nil;
            sendBGroup = nil;
            insertGroup = nil;
			"FX cleanup complete".postln;
        })
    }

    *register { |p|
        plugins = plugins.add(p);
        "registered %\n".postf(p);
    }

    *initClass {

        plugins = [];

        StartUp.add {

            OSCFunc.new({ |msg, time, addr, recvPort|
                FxSetup.alloc;
            }, "/fxmod/alloc");

            OSCFunc.new({ |msg, time, addr, recvPort|
                FxSetup.dynamicInit;
            }, "/fxmod/init");

            OSCFunc.new({ |msg, time, addr, recvPort|
                FxSetup.dynamicCleanup;
            }, "/fxmod/cleanup");

            SynthDef(\FxReplacer, {|inBus, outBus, drywet|
				XOut.ar(outBus, drywet, In.ar(inBus, 2));
			}).add;

			plugins.do { |p|
				"installing %\n".postf(p);
				p.addSynthdefs;
				p.listenOSC;
			};

        }
    }

}
