#!/usr/bin/env node
// Run against the package assembled by this repository's production build.
// It assumes the documented 1 ns near-boundary snapping policy, not exact preservation of sub-ns input.
import assert from 'node:assert/strict';
import {pathToFileURL} from 'node:url';
import {resolve} from 'node:path';
if (!process.argv[2]) throw new Error('Usage: node verify-release-carry.mjs /path/to/build/npm/index.mjs');
const m=await import(pathToFileURL(resolve(process.argv[2])).href);
const C=m.WesternZodiacCalculator?.getInstance?.() ?? m.WesternZodiacCalculator;
assert.equal(typeof C?.calculateHoroscope,'function','Raw calculator export missing');
assert.equal(typeof C?.calculateHoroscopeUtc,'function','Raw UTC export missing');
const records=[];
function check(name,fn) {try{fn();records.push({name,passed:true});}catch(e){records.push({name,passed:false,error:String(e)});}}
function compare(a,b) {
  for (const key of ['sun','moon','ascendant','midheaven']) {
    if (a[key]===null || b[key]===null) {assert.equal(a[key],b[key],`${key} nullability`);continue;}
    const x=a[key].totalLongitude,y=b[key].totalLongitude;
    assert.ok(Number.isFinite(x)&&Number.isFinite(y),`${key} non-finite longitude`);
    const raw=Math.abs(x-y)%360;
    assert.ok(Math.min(raw,360-raw)<1e-12,`${key} differs: ${x} vs ${y}`);
    assert.equal(a[key].sign,b[key].sign,`${key} sign mismatch`);
  }
  assert.equal(a.ascendantStatus,b.ascendantStatus,'Ascendant status mismatch');
  assert.equal(a.isPolarLatitude,b.isPolarLatitude,'Polar flag mismatch');
}
const cases=[
  {name:'original_plus1point1',local:[2026,1,2,1,6,0,1.1,0,0],utc:[2026,1,2,0,0,0,0,0]},
  {name:'near_midnight_zero_offset',local:[2026,1,2,23,59,59.999999999,0,0,0],utc:[2026,1,3,0,0,0,0,0]},
  {name:'near_midnight_minus1',local:[2026,1,2,22,59,59.999999999,-1,0,0],utc:[2026,1,3,0,0,0,0,0]},
  {name:'near_midnight_plus1',local:[2026,1,2,0,59,59.999999999,1,0,0],utc:[2026,1,2,0,0,0,0,0]},
  {name:'month_carry',local:[2026,1,31,23,59,59.999999999,0,0,0],utc:[2026,2,1,0,0,0,0,0]},
  {name:'year_carry',local:[2026,12,31,23,59,59.999999999,0,0,0],utc:[2027,1,1,0,0,0,0,0]},
  {name:'ordinary_plus7',local:[2026,1,1,2,0,0,7,0,0],utc:[2025,12,31,19,0,0,0,0]}
];
for(const c of cases) {
  const [year,month,day,hour,minute,second,utcOffsetHours,latitude,longitude]=c.local;
  const options={year,month,day,hour,minute,second,utcOffsetHours,latitude,longitude};
  check(`${c.name}/raw`,()=>compare(C.calculateHoroscope(...c.local),C.calculateHoroscopeUtc(...c.utc)));
  check(`${c.name}/wrapper_positional`,()=>{assert.equal(typeof m.calculateHoroscope,'function');compare(m.calculateHoroscope(...c.local),C.calculateHoroscopeUtc(...c.utc));});
  check(`${c.name}/wrapper_options`,()=>{assert.equal(typeof m.calculateHoroscope,'function');compare(m.calculateHoroscope(options),C.calculateHoroscopeUtc(...c.utc));});
}
// Optional seconds means omission; it should not silently coerce an explicit null into zero.
const base={year:2026,month:1,day:2,hour:0,minute:0,utcOffsetHours:0,latitude:0,longitude:0};
check('options_omitted_seconds',()=>compare(m.calculateHoroscope(base),C.calculateHoroscopeUtc(2026,1,2,0,0,0,0,0)));
check('options_null_seconds_rejected',()=>{assert.equal(typeof m.calculateHoroscope,'function');assert.throws(()=>m.calculateHoroscope({...base,second:null}));});
check('utc_options_null_seconds_rejected',()=>{assert.equal(typeof m.calculateHoroscopeUtc,'function');assert.throws(()=>m.calculateHoroscopeUtc({yearUtc:2026,monthUtc:1,dayUtc:2,hourUtc:0,minuteUtc:0,secondUtc:null,latitudeDeg:0,longitudeDeg:0}));});
const utcBase={yearUtc:2026,monthUtc:1,dayUtc:2,hourUtc:0,minuteUtc:0};
for (const [name,coords] of [
  ['both_missing',{}], ['latitude_missing',{longitudeDeg:0}],
  ['longitude_missing',{latitude:0}],
]) {
  check(`local_options_${name}_rejected`,()=>assert.throws(()=>m.calculateHoroscope({...base,latitude:undefined,longitude:undefined,...coords})));
  check(`utc_options_${name}_rejected`,()=>assert.throws(()=>m.calculateHoroscopeUtc({...utcBase,...coords})));
}
for (const [name,coords] of [
  ['short',{latitude:0,longitude:0}],
  ['degree',{latitudeDeg:0,longitudeDeg:0}],
  ['mixed_latitude',{latitude:0,longitudeDeg:0}],
  ['mixed_longitude',{latitudeDeg:0,longitude:0}],
]) {
  check(`local_options_${name}`,()=>compare(m.calculateHoroscope({...base,...coords}),C.calculateHoroscopeUtc(2026,1,2,0,0,0,0,0)));
  check(`utc_options_${name}`,()=>compare(m.calculateHoroscopeUtc({...utcBase,...coords}),C.calculateHoroscopeUtc(2026,1,2,0,0,0,0,0)));
}
const summary={total:records.length,passed:records.filter(r=>r.passed).length,failed:records.filter(r=>!r.passed).length};
console.log(JSON.stringify({summary,records},null,2));process.exitCode=summary.failed?1:0;
