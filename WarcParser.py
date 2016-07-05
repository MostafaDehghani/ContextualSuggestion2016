import argparse
from bs4 import BeautifulSoup, Comment
import re, copy
import codecs
import os
import fnmatch
import warcat.model
import sys  


def recursive_glob(treeroot, pattern):
    results = []
    for base, dirs, files in os.walk(treeroot):
        goodfiles = fnmatch.filter(files, pattern)
        results.extend(os.path.join(base, f) for f in goodfiles)
    return results

# def html_part(s):
#   first, last = '<html', 'html>'
#   if first not in s or last not in s:
#     print("malform html")
#     return s
#   result = re.search('%s(.*)%s' % (first, last), s).group(0)
#   return result

def html_part(s,file):
  first, last = '<html', '/html>'
  if first not in s.lower() or last not in s.lower():
    print("malform html...")
    return s
  try:
      start = s.lower().index(first) + len(first)
      end = s.lower().index(last, start)
      return first + s[start:end] + last
  except ValueError:
      raise
  
def create_file_dir(filename):
  if not os.path.exists(os.path.dirname(filename)):
    try:
        os.makedirs(os.path.dirname(filename))
    except OSError as exc: # Guard against race condition
        if exc.errno != errno.EEXIST:
            raise
  
def remove_comment(text):
  return re.sub(r'<!--.*?-->','',text, flags=re.DOTALL)

def visible(element):
  if element.parent.name in ['style', 'script', '[document]', 'head', 'title']:
    return False
  return True

def get_txt(text,file_name):
  text = html_part(text, file_name)
  final_text = text
  try:
    soup = BeautifulSoup(text, "html.parser")
    [s.extract() for s in soup(['style', 'script', '[document]', 'head', 'title'])]
#     final_text = filter(lambda x: len(x.split()) > 3, soup.strings)
#     final_text = remove_comment(' '.join(final_text))
    final_text = soup.get_text()
    final_text = ' '.join(final_text.split())
  except:
    print('non parsable html in: ', file_name)
  return final_text #.encode('utf-8')
  

# parser = argparse.ArgumentParser(description="Parse warc files")
# parser.add_argument('input', metavar='input', type=str, help='input dir')
# parser.add_argument('output', metavar='output', type=str, help='output dir')
# args = parser.parse_args()

# input_dir = args.input
# output_dir = args.output

input_dir = 'crawls'
output_dir = 'cleaned'
print('loading files....')
files = recursive_glob(os.path.join(input_dir),'*.warc.gz')
print('num of files in the input dir:', len(fils))
cnt = 0
for file in files:
  cnt += 1
  id_flag = False
  html_flag = False
  warc = warcat.model.WARC()
  print(cnt, " -> processing " + file)
  try:
    warc.load(file)
  except:
    print('non loadable warc file: ', file)
    continue
  cleaned_txt = ''
  for record in warc.records:
    if record.warc_type == 'warcinfo': 
      trec_id = record.content_block.fields['id']
      id_flag = True
    if record.warc_type == 'response':
      if not id_flag or trec_id == "":
        print('no trec id is given for file: ' + file)
        trec_id = os.path.basename(file)
      if 'Content-Type' in record.content_block.fields and 'text/html' in record.content_block.fields['Content-Type']:
        path = os.path.join(output_dir,os.path.relpath(os.path.dirname(file), input_dir),trec_id +'.txt')
        create_file_dir(path)
        txt_file = codecs.open(path, "w",encoding='utf-8', errors='ignore')
        html_flag = True
        unlcean_srt = ''
        for b in record.content_block.payload.iter_bytes():
          unlcean_srt += '\n' + str(b,'utf-8',errors='ignore')
        if len(unlcean_srt) < 5:
          continue
        cleaned_txt += get_txt(unlcean_srt.strip(), file)
  if html_flag:
    if len(cleaned_txt) < 5:
      print('textual content less thatn 5 char in: ' + file)
    txt_file.write(cleaned_txt)
    txt_file.close()
#     print(trec_id + " is processed..." )
  else:
    print('no html content is given for file: ' + file)
print("DONE!")
      
