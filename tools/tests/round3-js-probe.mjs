#!/usr/bin/env node
// Run against the package assembled by this repository's production build.
import { pathToFileURL } from 'node:url';
import { resolve } from 'node:path';
if (!process.argv[2]) throw new Error('Usage: node js_probe.mjs /path/to/module.mjs');
const mod = await import(pathToFileURL(resolve(process.argv[2])).href);
const C=mod.WesternZodiacCalculator.getInstance?.() ?? mod.WesternZodiacCalculator;
const S=mod.WesternZodiacSign; const P=mod.ZodiacPosition;
const sf=S.Companion ?? S; const pf=P.Companion ?? P;
const records=[];
function check(name, fn, valid) {
  let v;
  try {v=fn(); records.push({name,passed:valid(v),returned: typeof v==='object' && v!==null ? {longitude:v.totalLongitude,degreeInSign:v.degreeInSign,second:v.second,sign:v.sign?.englishName}:v});}
  catch(e){records.push({name,passed:false,error:e.message});}
}
function rejects(name,fn) {
  try {let v=fn();records.push({name,passed:false,accepted:{longitude:v?.totalLongitude,degreeInSign:v?.degreeInSign,second:v?.second,sign:v?.sign?.englishName}});}
  catch(e) {records.push({name,passed:true,error:e.message});}
}
const bad=[['null',null],['false',false],['true',true],['emptyString',''],['zeroString','0'],['decimalString','12.5'],['emptyArray',[]],['zeroArray',[0]],['object',{}],['coercibleObject',{valueOf:()=>12.5}],['boxedNumber',new Number(0)],['NaN',NaN],['Infinity',Infinity],['negativeInfinity',-Infinity]];
const utc=[2026,4,14,3,30,0,11.5564,104.9282];
const local=[2026,4,14,10,30,0,7,11.5564,104.9282];
for (let i=0;i<utc.length;i++) for (const [n,b] of bad) {const a=utc.slice();a[i]=b;rejects(`utc[${i}]/${n}`,()=>C.calculateHoroscopeUtc(...a));}
for (let i=0;i<local.length;i++) for (const [n,b] of bad) {const a=local.slice();a[i]=b;rejects(`local[${i}]/${n}`,()=>C.calculateHoroscope(...a));}
for (const [n,b] of bad) rejects(`factory/${n}`,()=>pf.fromLongitude(b));
for (const [n,b] of [...bad,['fraction',.5],['overflow',4294967296]]) rejects(`index/${n}`,()=>sf.fromIndex(b));
check('default_seconds',()=>C.calculateHoroscopeUtc(2026,4,14,3,30,undefined,11.5564,104.9282),v=>Number.isFinite(v.sun.totalLongitude));
check('factory_valid_250',()=>pf.fromLongitude(250),v=>v.sign===S.SAGITTARIUS && v.degreeInSign===10);
check('factory_wrap_negative',()=>pf.fromLongitude(-110),v=>v.totalLongitude===250);
rejects('model/negative_fields',()=>new P(S.ARIES,-.000001,0,0,-.00001,0));
const near30=pf.fromLongitude(29.999999);
rejects('model/degree30',()=>new P(S.ARIES,30,near30.wholeDegree,near30.minute,near30.second,near30.totalLongitude));
const near60=pf.fromLongitude(59.99995/3600);
rejects('model/second60',()=>new P(S.ARIES,near60.degreeInSign,near60.wholeDegree,near60.minute,60,near60.totalLongitude));
rejects('model/fakeSign',()=>new P({index:0,englishName:'Not a ZodiacSign'},0,0,0,0,0));
check('freeze/position',()=>pf.fromLongitude(250),v=>Object.isFrozen(v));
check('freeze/sign',()=>S.ARIES,v=>Object.isFrozen(v));
check('freeze/status',()=>mod.AscendantStatus.CALCULATED,v=>Object.isFrozen(v));
const validChart=C.calculateHoroscopeUtc(...utc);
for (const [name,status] of [
  ['fake',{code:'CALCULATED'}], ['null',null],
]) rejects(`model/ascendantStatus_${name}`,()=>new mod.WesternHoroscope(
  validChart.sun,validChart.moon,validChart.ascendant,validChart.midheaven,
  validChart.isPolarLatitude,status
));
const zero=C.calculateHoroscopeUtc(2026,1,2,0,0,0,0,0);
check('offset/UTC_plus_1point1',()=>C.calculateHoroscope(2026,1,2,1,6,0,1.1,0,0),v=>Math.abs(((v.ascendant.totalLongitude-zero.ascendant.totalLongitude+540)%360)-180)<1e-6);
const summary={total:records.length,passed:records.filter(r=>r.passed).length,failed:records.filter(r=>!r.passed).length};
console.log(JSON.stringify({summary,records},null,2));
process.exitCode=summary.failed?1:0;
